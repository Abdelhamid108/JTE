// steps/infracost.groovy
//
// Production FinOps Cost Estimation & Automated GitHub Commenting.
// Executes inside the official Docker agent: infracost/infracost:ci-0.10

void call() {
    String targetDir       = config.infra_dir ?: '.'
    String repoName        = config.github_repo 
    String infracostCredId = config.infracost_api_key ?: 'infracost-api-key'
    String githubCredId    = config.github_token ?: 'github-token'
    def costLimit          = config.monthly_cost_limit
    boolean softFail       = (config.infracost_soft_fail != null) ? config.infracost_soft_fail : (config.softFail ?: false)

    echo "infracost: analyzing infrastructure costs for '${repoName}' in '${targetDir}'..."

    withCredentials([
        string(credentialsId: infracostCredId, variable: 'INFRACOST_API_KEY'),
        string(credentialsId: githubCredId, variable: 'GITHUB_TOKEN')
    ]) {
        dir(targetDir) {
            // 1. Run Infracost breakdown
            generateCostBreakdown()

            // 2. Post comment to GitHub PR or commit
            postGitHubComment(repoName)

            // 3. Archive cost artifacts
            archiveCostReports()

            // 4. Enforce monthly cost threshold (if defined)
            enforceCostPolicy(costLimit, softFail)
        }
    }
}

// ── Helper Methods ────────────────────────────────────────────────────────────

void generateCostBreakdown() {
    echo "infracost: generating cost breakdown..."
    sh 'infracost --version'
    sh 'infracost breakdown --path . --format table --no-color | tee infracost-summary.txt'
    sh 'infracost breakdown --path . --format json --out-file infracost.json'
}

void postGitHubComment(String repoName) {
    String prNumber  = env.CHANGE_ID
    String commitSha = env.GIT_COMMIT ?: sh(script: 'git rev-parse HEAD 2>/dev/null || true', returnStdout: true).trim()

    if (prNumber) {
        echo "infracost: posting cost breakdown to Pull Request #${prNumber}..."
        sh """
            infracost comment github \
                --path=infracost.json \
                --repo="${repoName}" \
                --pull-request="${prNumber}" \
                --github-token="\$GITHUB_TOKEN" \
                --behavior=update
        """
    } else if (commitSha) {
        echo "infracost: posting cost breakdown to Commit ${commitSha}..."
        sh """
            infracost comment github \
                --path=infracost.json \
                --repo="${repoName}" \
                --commit="${commitSha}" \
                --github-token="\$GITHUB_TOKEN" \
                --behavior=update
        """
    } else {
        echo "infracost: no PR number or Commit SHA found — skipping GitHub comment."
    }
}

void archiveCostReports() {
    echo "infracost: archiving cost reports..."
    archiveArtifacts artifacts: 'infracost-summary.txt, infracost.json', allowEmptyArchive: false
}

void enforceCostPolicy(def costLimit, boolean softFail) {
    if (costLimit == null) {
        return
    }

    echo "infracost: evaluating FinOps spend policy (Limit: \$${costLimit})..."

    String totalCostStr = sh(
        script: '''#!/bin/bash
            python3 -c "import json; d=json.load(open('infracost.json')); print(d.get('totalMonthlyCost') or d.get('diffTotalMonthlyCost') or 0)" 2>/dev/null || \
            jq -r '(.totalMonthlyCost // .diffTotalMonthlyCost // 0)' infracost.json 2>/dev/null || echo "0"
        ''',
        returnStdout: true
    ).trim()

    try {
        float totalCost = Float.parseFloat(totalCostStr)
        float limit     = Float.parseFloat(costLimit.toString())

        echo "infracost: Projected Total Monthly Cost = \$${totalCost} (Policy Limit: \$${limit})"

        if (totalCost > limit) {
            String msg = "FinOps Policy Violation: Projected monthly cost (\$${totalCost}) exceeds policy limit (\$${limit})."
            if (softFail) {
                echo "infracost: WARNING — ${msg} (softFail=true, continuing pipeline)"
                currentBuild.result = 'UNSTABLE'
            } else {
                error msg
            }
        } else {
            echo "infracost: [PASS] Projected cost is within policy threshold (\$${totalCost} <= \$${limit})."
        }
    } catch (NumberFormatException nfe) {
        echo "infracost: WARNING — could not parse cost '${totalCostStr}': ${nfe.message}"
    }
}
