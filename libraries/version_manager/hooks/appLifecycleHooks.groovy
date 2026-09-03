// hooks/appLifecycleHooks.groovy — JTE Lifecycle Hooks for Version Management & Strict Quality Governance (DISABLED)

// ─────────────────────────────────────────────────────────────
// PRE-STEP: Strict Quality, Security & Version Gates (DISABLED)
// ─────────────────────────────────────────────────────────────
@BeforeStep
void onBeforeStep() {
    // Disabled for now
    return
}

// ─────────────────────────────────────────────────────────────
// POST-STEP: S3 version registration (DISABLED)
// ─────────────────────────────────────────────────────────────
@AfterStep
void onAfterStep() {
    // Disabled for now
    return
}

