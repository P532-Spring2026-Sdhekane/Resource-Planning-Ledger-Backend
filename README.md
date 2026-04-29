# Resource Planning Ledger

[![CI](https://github.com/YOUR_USERNAME/resource-planning-ledger/actions/workflows/ci.yml/badge.svg)](https://github.com/YOUR_USERNAME/resource-planning-ledger/actions/workflows/ci.yml)

**Live URL:**

A resource planning and double-entry ledger system built for CSCI-P532 (Spring 2026). Plans, tracks, and audits resource allocation across a portfolio of work plans using four classic OO design patterns.

---

## Running locally

### With Docker Compose (recommended)

```bash
git clone https://github.com/YOUR_USERNAME/resource-planning-ledger.git
cd resource-planning-ledger
docker compose up --build
```

Then open http://localhost:8080

## Render.com Setup

1. Create a **Web Service** → Docker → port 8080
2. Create a **PostgreSQL** database (free tier)
3. Copy the Internal Database URL → env var `SPRING_DATASOURCE_URL`
4. Set `SPRING_DATASOURCE_USERNAME` and `SPRING_DATASOURCE_PASSWORD`
5. Copy the deploy-hook URL → GitHub secret `RENDER_DEPLOY_HOOK`

---

## API Reference

| Method   | Path                            | Description                            |
| -------- | ------------------------------- | -------------------------------------- |
| GET/POST | `/api/protocols`                | List / create protocols                |
| POST     | `/api/protocols/{id}/steps`     | Add a step to a protocol               |
| GET/POST | `/api/resource-types`           | List / create resource types           |
| POST     | `/api/plans`                    | Create plan (from scratch or protocol) |
| GET      | `/api/plans/{id}`               | Plan tree with statuses                |
| GET      | `/api/plans/{id}/report`        | Depth-first summary report             |
| POST     | `/api/plans/{id}/actions`       | Add an action to a plan                |
| POST     | `/api/actions/{id}/implement`   | Trigger implement()                    |
| POST     | `/api/actions/{id}/complete`    | Trigger complete()                     |
| POST     | `/api/actions/{id}/suspend`     | Trigger suspend()                      |
| POST     | `/api/actions/{id}/resume`      | Trigger resume()                       |
| POST     | `/api/actions/{id}/abandon`     | Trigger abandon()                      |
| POST     | `/api/actions/{id}/allocations` | Attach resource allocation             |
| GET      | `/api/accounts`                 | All accounts with balances             |
| GET      | `/api/accounts/{id}/entries`    | Ledger entries for account             |
| GET      | `/api/audit-log`                | Full audit log                         |
