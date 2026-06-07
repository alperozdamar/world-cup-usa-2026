# Deploy — World Cup 2026

Same pattern as [my-finance-watcher](https://github.com/alperozdamar/my-finance-watcher): GitHub Actions → Docker Hub → EC2 SSH.

## Architecture on EC2

```
EC2 (54.242.205.198)
├── mysql-standalone   :3306   (MySQL 8, DB: worldcup)
└── world-cup-usa-2026 :8090   (Spring Boot app)
```

Finance app (`my-finance-watcher`) can stay **stopped** while World Cup runs — they share MySQL but use different databases.

---

## One-time setup

### 1. EC2 — MySQL running

```bash
docker ps   # mysql-standalone should be Up
docker exec mysql-standalone mysql -uroot -p'password' -e "SHOW DATABASES;"
# worldcup must exist
```

Create MySQL 8 if needed:

```bash
docker run -d --name mysql-standalone \
  --restart unless-stopped \
  -e MYSQL_ROOT_PASSWORD='password' \
  -e MYSQL_DATABASE=worldcup \
  -p 3306:3306 \
  mysql:8
```

### 2. EC2 — Seed login tables (first time only)

Hibernate creates app tables (`matches`, `predictions`, etc.) on startup, but **not** Spring Security tables (`users`, `authorities`). Without this step the app crash-loops on first boot.

From your **Mac** (repo root), copy `db/setup.sql` to EC2:

```bash
scp -i Alper_DevOps2_Key.pem db/setup.sql \
  ec2-user@54.242.205.198:/home/ec2-user/setup.sql
```

On **EC2**, load into the `worldcup` database:

```bash
docker exec -i mysql-standalone mysql -uroot -p'password' worldcup \
  < /home/ec2-user/setup.sql
```

Verify:

```bash
docker exec mysql-standalone mysql -uroot -p'password' worldcup \
  -e "SHOW TABLES; SELECT username FROM users;"
```

You should see `users`, `authorities`, and 7 players. Safe to re-run (`INSERT IGNORE` / `CREATE IF NOT EXISTS`).

Source of truth: [`db/setup.sql`](db/setup.sql)

### 3. EC2 — Security group

Allow inbound **TCP 8090** from your IP (or `0.0.0.0/0` if you accept public access).

AWS Console → EC2 → Security Groups → inbound rule:

| Type | Port | Source |
|------|------|--------|
| Custom TCP | 8090 | Your IP / 0.0.0.0/0 |

### 4. GitHub repository secrets

Repo: **alperozdamar/world-cup-usa-2026** → Settings → Secrets and variables → Actions

| Secret | Value |
|--------|--------|
| `DOCKERHUB_USERNAME` | `alperoz` (same as finance app) |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_HOST` | `54.242.205.198` |
| `EC2_USER` | `ec2-user` |
| `EC2_SSH_KEY` | Full private key contents (`develop.alper4_KeyPair.pem`) |
| `MYSQL_ROOT_PASSWORD` | `password` |

**Optional — daily reminder emails** (omit all of these to leave mail disabled; deploy still succeeds):

| Secret | Value |
|--------|--------|
| `APP_MAIL_ENABLED` | `true` |
| `APP_BASE_URL` | `http://54.242.205.198:8090` |
| `APP_MAIL_FROM` | `World Cup 2026 <alper.ozdamar@gmail.com>` |
| `SPRING_MAIL_USERNAME` | `alper.ozdamar@gmail.com` |
| `SPRING_MAIL_PASSWORD` | Gmail **App Password** (16 characters — not your login password) |

When `APP_MAIL_ENABLED` is `true`, all five mail secrets above must be set or the deploy job fails validation. Gmail SMTP host/port are configured in the workflow. To turn mail off later, delete `APP_MAIL_ENABLED` or set it to anything other than `true`.

You can copy `DOCKERHUB_*`, `EC2_*` from the **my-finance-watcher** repo secrets if already set.

### 5. SSH key access

The EC2 user must run Docker without sudo. If deploy fails with permission denied:

```bash
sudo usermod -aG docker ec2-user
# log out and back in, or use root in EC2_USER secret
```

---

## Deploy (automatic)

Push to **`main`**:

```bash
git add .
git commit -m "Prepare production deploy"
git push origin main
```

GitHub Actions will:

1. Run tests + Maven build  
2. Build & push Docker image to Docker Hub  
3. SSH to EC2, pull image, restart container  

Watch progress: GitHub → Actions tab.

---

## Manual deploy (on EC2)

If you need to deploy without CI:

```bash
docker pull alperoz/world-cup-usa-2026:latest

docker stop world-cup-usa-2026 || true
docker rm world-cup-usa-2026 || true

docker run -d --name world-cup-usa-2026 \
  --restart unless-stopped \
  --memory=384m \
  --link mysql-standalone:mysql \
  -p 8090:8090 \
  -e JAVA_TOOL_OPTIONS='-Xmx256m -Xms128m' \
  -e SPRING_DATASOURCE_URL='jdbc:mysql://mysql:3306/worldcup?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true' \
  -e SPRING_DATASOURCE_USERNAME=root \
  -e SPRING_DATASOURCE_PASSWORD='password' \
  alperoz/world-cup-usa-2026:latest
```

---

## Verify

```bash
docker ps
docker logs world-cup-usa-2026 --tail 30
curl -I http://localhost:8090/showMyLoginPage
```

Browser: **http://54.242.205.198:8090**

Login: `alper` / `123` (change password in Profile after first login)

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Connection refused on 8090 | Open security group port 8090 |
| App crash-loop on first deploy | Run [`db/setup.sql`](db/setup.sql) into `worldcup` (see step 2 above) |
| App crash-loop / OOM | Add **1 GB swap** (below); keep finance app stopped; use `-Xmx128m` |
| DB connection error | `docker start mysql-standalone`; check `MYSQL_ROOT_PASSWORD` secret |
| `Access denied for user` | Env vars must use `root` / `password` / host `mysql` |
| Disk 100% full | `docker system prune -af`; expand EBS to 20 GB |

### Add swap on EC2 (recommended for t3.micro)

```bash
sudo dd if=/dev/zero of=/swapfile bs=1M count=1024
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile swap swap defaults 0 0' | sudo tee -a /etc/fstab
free -h
```

---

## Stop / restart

```bash
docker stop world-cup-usa-2026      # stop app, keep MySQL
docker start world-cup-usa-2026       # start again
```

Redeploy: push to `main` or re-run the **Deploy to EC2** job in GitHub Actions.

---

## Optional — daily reminder emails

Reminders go to users with an email on their profile (Profile → Settings, or set manually in `user_profiles.email`).

**Recommended:** add the optional mail secrets in [step 4](#4-github-repository-secrets). Every deploy to `main` passes them to the container automatically.

**Manual fallback** (only if you are not using GitHub secrets): SSH to EC2 and add the `-e APP_MAIL_*` / `-e SPRING_MAIL_*` flags to `docker run` as in older deploy notes.

Default schedule: **14:00 UTC** daily (`app.reminder.cron`). Emails are sent only for **missing** group 1st/2nd picks (while group is still open) and **missing final pick** (before tournament kickoff).
