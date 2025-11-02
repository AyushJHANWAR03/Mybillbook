# Deployment Guide

## Backend Deployment (Railway)

### Prerequisites
1. Create a Railway account at https://railway.app
2. Install Railway CLI: `npm install -g @railway/cli`

### Steps

1. **Login to Railway**
   ```bash
   railway login
   ```

2. **Initialize Railway Project**
   ```bash
   cd /Users/ayush/mybillbook
   railway init
   ```

3. **Add PostgreSQL Database**
   - Go to Railway dashboard
   - Click "New" → "Database" → "Add PostgreSQL"
   - Railway will auto-create DB credentials

4. **Set Environment Variables**
   In Railway dashboard, add these variables:
   ```
   OPENAI_API_KEY=your-openai-api-key
   OPENAI_MODEL=gpt-4o-mini
   CORS_ALLOWED_ORIGINS=https://your-netlify-app.netlify.app
   ```

   Railway automatically provides these (from PostgreSQL plugin):
   ```
   PGHOST (maps to DB_HOST)
   PGPORT (maps to DB_PORT)
   PGDATABASE (maps to DB_NAME)
   PGUSER (maps to DB_USERNAME)
   PGPASSWORD (maps to DB_PASSWORD)
   ```

5. **Update application.yml** (if needed)
   Make sure your application.yml uses environment variables:
   ```yaml
   spring:
     datasource:
       url: jdbc:postgresql://${DB_HOST:${PGHOST}}:${DB_PORT:${PGPORT}}/${DB_NAME:${PGDATABASE}}
       username: ${DB_USERNAME:${PGUSER}}
       password: ${DB_PASSWORD:${PGPASSWORD}}
   ```

6. **Deploy**
   ```bash
   railway up
   ```

7. **Get Your Backend URL**
   ```bash
   railway domain
   ```
   Example: `https://mybillbook-production.railway.app`

---

## Frontend Deployment (Netlify)

### Prerequisites
1. Create a Netlify account at https://netlify.com
2. Install Netlify CLI: `npm install -g netlify-cli`

### Steps

1. **Build the Frontend**
   ```bash
   cd /Users/ayush/mybillbook/frontend
   npm run build
   ```

2. **Login to Netlify**
   ```bash
   netlify login
   ```

3. **Initialize Netlify**
   ```bash
   netlify init
   ```
   - Choose "Create & configure a new site"
   - Select your team
   - Enter site name (e.g., `mybillbook-ai`)
   - Build command: `npm run build`
   - Publish directory: `dist`

4. **Set Environment Variables**
   In Netlify dashboard → Site settings → Environment variables:
   ```
   VITE_API_BASE_URL=https://your-railway-app.railway.app/api
   ```

   Or via CLI:
   ```bash
   netlify env:set VITE_API_BASE_URL https://your-railway-app.railway.app/api
   ```

5. **Deploy**
   ```bash
   netlify deploy --prod
   ```

6. **Get Your Frontend URL**
   Example: `https://mybillbook-ai.netlify.app`

---

## Post-Deployment

### 1. Update CORS on Backend
After deploying frontend, update Railway environment variable:
```
CORS_ALLOWED_ORIGINS=https://your-netlify-app.netlify.app
```

### 2. Test the Application
1. Open your Netlify URL
2. Click "Fill Demo Credentials"
3. Login
4. Upload invoices and payments
5. Run AI reconciliation
6. Confirm suggestions

### 3. Monitor Logs
**Railway:**
```bash
railway logs
```

**Netlify:**
```bash
netlify logs
```

---

## Troubleshooting

### Backend Issues
- **Database connection failed**: Check Railway PostgreSQL plugin is added
- **OpenAI errors**: Verify OPENAI_API_KEY is set correctly
- **CORS errors**: Ensure CORS_ALLOWED_ORIGINS matches your Netlify URL

### Frontend Issues
- **API calls failing**: Check VITE_API_BASE_URL points to Railway backend
- **Build fails**: Run `npm install` and `npm run build` locally first
- **Blank page**: Check browser console for errors

---

## Continuous Deployment

### Railway (Auto-deploy on push)
```bash
railway link
git push origin main
```
Railway automatically deploys on every push to main branch.

### Netlify (Auto-deploy on push)
```bash
netlify link
git push origin main
```
Netlify automatically deploys on every push to main branch.

---

## Local Testing with Production URLs

Create `.env.local` in frontend:
```
VITE_API_BASE_URL=https://your-railway-app.railway.app/api
```

Run locally:
```bash
npm run dev
```

This tests frontend against production backend.
