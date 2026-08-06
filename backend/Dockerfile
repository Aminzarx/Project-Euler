# Multi-stage build: compile TypeScript in a full node image, run the compiled JS in a lean one.

FROM node:22-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY prisma ./prisma
RUN npx prisma generate
COPY tsconfig.json ./
COPY src ./src
RUN npm run build

FROM node:22-alpine AS runner
WORKDIR /app
ENV NODE_ENV=production
COPY package.json package-lock.json ./
RUN npm ci --omit=dev
COPY --from=builder /app/node_modules/.prisma ./node_modules/.prisma
COPY --from=builder /app/node_modules/@prisma ./node_modules/@prisma
COPY --from=builder /app/dist ./dist
COPY prisma ./prisma

EXPOSE 4000
# Applies any pending migrations before every start — safe to run on every boot since Prisma
# migrations are idempotent (already-applied ones are skipped).
CMD ["sh", "-c", "npx prisma migrate deploy && node dist/server.js"]
