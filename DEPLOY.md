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

### 2. EC2 — Security group

Allow inbound **TCP 8090** from your IP (or `0.0.0.0/0` if you accept public access).

AWS Console → EC2 → Security Groups → inbound rule:

| Type | Port | Source |
|------|------|--------|
| Custom TCP | 8090 | Your IP / 0.0.0.0/0 |

### 3. GitHub repository secrets

Repo: **alperozdamar/world-cup-usa-2026** → Settings → Secrets and variables → Actions

| Secret | Value |
|--------|--------|
| `DOCKERHUB_USERNAME` | `alperoz` (same as finance app) |
| `DOCKERHUB_TOKEN` | Docker Hub access token |
| `EC2_HOST` | `54.242.205.198` |
| `EC2_USER` | `ec2-user` |
| `EC2_SSH_KEY` | Full private key contents (`develop.alper4_KeyPair.pem`) |
| `MYSQL_ROOT_PASSWORD` | `password` |

You can copy `DOCKERHUB_*`, `EC2_*` from the **my-finance-watcher** repo secrets if already set.

### 4. SSH key access

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
  --link mysql-standalone:mysql \
  -p 8090:8090 \
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
| App crash-loop / OOM | Stop finance app; consider t3.small or add swap |
| DB connection error | `docker start mysql-standalone`; check `MYSQL_ROOT_PASSWORD` secret |
| `Access denied for user` | Env vars must use `root` / `password` / host `mysql` |

---

## Stop / restart

```bash
docker stop world-cup-usa-2026      # stop app, keep MySQL
docker start world-cup-usa-2026       # start again
```

Redeploy: push to `main` or re-run the **Deploy to EC2** job in GitHub Actions.
