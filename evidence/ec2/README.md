# EC2 Live Verification Evidence

This folder stores archived evidence from live EC2 verification runs.

## Generate evidence

Run from project root:

```bash
chmod +x deploy/ec2/live-verify-archive.sh
./deploy/ec2/live-verify-archive.sh http://<EC2_PUBLIC_IP>:8080
```

## Output structure

Each run creates a timestamped folder:

- `verify-deploy.log` : full output from `deploy/ec2/verify-deploy.sh`
- `metadata.txt` : run metadata (timestamp, URL, commit, status)
- `screenshot-swagger-ui.png` / `screenshot-home.png` : optional auto screenshots (if Chromium is available)

If screenshots are not auto-captured, take a manual screenshot and put it in the same run folder.
