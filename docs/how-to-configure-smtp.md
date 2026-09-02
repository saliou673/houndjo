# How to configure SMTP

The backend SMTP variables are configured in:

- `backend/houndjo-restapi/.env`

## Option 1: MailDev (local development)

MailDev runs from `docker/docker-compose.yml`.

1. Start containers:

```bash
docker compose -f docker/docker-compose.yml up -d
```

2. Set these variables in `backend/houndjo-restapi/.env`:
    - `SMTP_HOST=localhost`
    - `SMTP_PORT=1025`
    - `MAIL_USERNAME=`
    - `MAIL_PASSWORD=`
    - `NO_REPLY_EMAIL=no-reply@houndjo.local`
3. Open MailDev UI to inspect sent emails:
    - `http://localhost:1080`

## Option 2: Gmail SMTP

1. [Create and manage your Gmail app passwords](https://myaccount.google.com/apppasswords). If the page is not
   available, enable 2-factor authentication first.
2. Copy the app password and set `MAIL_PASSWORD` to it.
3. Set these variables in `backend/houndjo-restapi/.env`:
    - `SMTP_HOST=smtp.gmail.com`
    - `SMTP_PORT=587`
    - `MAIL_USERNAME=your-gmail-address@gmail.com`
    - `MAIL_PASSWORD=your-gmail-app-password`
    - `NO_REPLY_EMAIL=your-gmail-address@gmail.com`
