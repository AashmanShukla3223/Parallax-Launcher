# Firebase Test Lab setup (one-time)

Physical-device verification for Parallax. This is the only automated path that exercises
real hardware — the emulator job covers composition and crashes, but not the GPU, sensors, or
the notification-listener / role plumbing.

## Why not a local emulator

The dev machines have no `/dev/kvm` (`systemd-detect-virt` reports `kvm`, meaning the box is
*already* a guest, and `vmx|svm` is absent from `/proc/cpuinfo`, so nested virt is not
exposed). Combined with 2.7 GiB RAM and no GPU, a local AVD is not viable. Hence cloud.

## Free quota (Spark plan, no card required)

- 5 physical-device runs per day
- 10 virtual-device runs per day
- Android Device Streaming: 30 min per month (Blaze-gated)

Blaze raises physical to 30 min/day free, then $5/device-hour. Stay on Spark and keep this
workflow on `workflow_dispatch` plus the weekly schedule; do not add it to `pull_request`,
or a burst of pushes silently burns the daily quota.

## 1. Pick a project

Any existing GCP project will do. If the project is not yet on Firebase, enable the API:

```bash
export PROJECT_ID=<your-project-id>
gcloud services enable testing.googleapis.com cloudtesting.googleapis.com \
  firebase.googleapis.com --project "$PROJECT_ID"
```

## 2. Create a service account and bind it to GitHub via WIF

Prefer Workload Identity Federation over a long-lived JSON key.

```bash
export PROJECT_ID=<your-project-id>
export REPO=AashmanShukla3223/Parallax-Launcher
export SA=parallax-testlab@$PROJECT_ID.iam.gserviceaccount.com

gcloud iam service-accounts create parallax-testlab --display-name "Parallax Test Lab" \
  --project "$PROJECT_ID"

# Launch runs, read results, and consume the serviceusage quota Test Lab requires.
for ROLE in roles/firebase.testAdmin roles/serviceusage.serviceUsageConsumer; do
  gcloud projects add-iam-policy-binding "$PROJECT_ID" \
    --member "serviceAccount:$SA" --role "$ROLE"
done

gcloud iam workload-identity-pools create github --location global --project "$PROJECT_ID"
gcloud iam workload-identity-pools providers create-oidc github \
  --location=global --workload-identity-pool=github \
  --project="$PROJECT_ID" \
  --issuer-uri="https://token.actions.githubusercontent.com" \
  --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
  --attribute-condition="assertion.repository == '$REPO'"
```

Note the `--attribute-condition`: it restricts the pool to this one repository.

## 3. Repository variables

Set these under **Settings → Secrets and variables → Actions → Variables**:

| Variable | Value |
|---|---|
| `GCP_PROJECT_ID` | your project id |
| `GCP_WIF_PROVIDER` | `projects/<num>/locations/global/workloadIdentityPools/github/providers/github` |
| `GCP_TESTLAB_SERVICE_ACCOUNT` | `parallax-testlab@<project-id>.iam.gserviceaccount.com` |
| `GCP_RESULTS_BUCKET` | a GCS bucket name for logs/screenshots |

Find the provider's full resource name with:

```bash
gcloud iam workload-identity-pools providers describe github \
  --location=global --workload-identity-pool=github --project="$PROJECT_ID"
```

Create the results bucket once:

```bash
gcloud storage buckets create gs://$BUCKET --location=US --project="$PROJECT_ID"
```

## 4. Results bucket access

The service account must be able to write to the bucket:

```bash
gcloud storage buckets add-iam-policy-binding "gs://$BUCKET" \
  --member "serviceAccount:parallax-testlab@$PROJECT_ID.iam.gserviceaccount.com" \
  --role roles/storage.objectAdmin
```

## Running it

Workflow: **Device Verification (Firebase Test Lab)** → *Run workflow*.

Before trusting a failure, confirm the device model is still in the fleet — Test Lab rotates
hardware, and a retired model looks like a real error:

```bash
gcloud firebase test android models list --project "$PROJECT_ID"
```

## What this does and does not prove

`ModeSmokeTest` asserts each of the seven modes reaches `RESUMED` and draws its content view.
That catches real regressions — a `lateinit` crash, a bad `SettingsRepository` key, a Canvas
draw that throws — and it needs the `onboarded` flag seeded, since `MainActivity` otherwise
shows onboarding instead of any mode screen.

It does **not** validate visual fidelity, haptic feel, or the notification routing described
in the root `AGENTS.md`. Mode 7's flip-state and keypad layout still want a human with a
device. Nothing here changes the local build story: `assembleDebug` plus
`aapt2 dump badging` and `apksigner verify` remain the local checks.