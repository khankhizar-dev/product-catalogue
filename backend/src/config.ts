import { z } from 'zod';

const EnvSchema = z.object({
  PORT: z.coerce.number().int().positive().default(3000),
  FAKE_STORE_BASE_URL: z.string().url().default('https://fakestoreapi.com'),
  /** Comma-separated product ids chosen as "top rated" for the home page. */
  FEATURED_PRODUCT_IDS: z.string().default('1,2,3,4,5'),
  TOP_RATED_PER_CATEGORY: z.coerce.number().int().positive().default(3),
  SALES_INBOX_EMAIL: z.string().email().default('sales@example.com'),
  EMAIL_FROM: z.string().default('Catalogue <no-reply@example.com>'),
  SMTP_HOST: z.string().default('localhost'),
  SMTP_PORT: z.coerce.number().int().positive().default(1025),
  SMTP_USER: z.string().optional(),
  SMTP_PASS: z.string().optional(),
  ENQUIRY_RATE_LIMIT_PER_MINUTE: z.coerce.number().int().positive().default(5),
});

export interface Config {
  port: number;
  fakeStoreBaseUrl: string;
  featuredProductIds: string[];
  topRatedPerCategory: number;
  salesInboxEmail: string;
  emailFrom: string;
  smtp: { host: string; port: number; user?: string; pass?: string };
  enquiryRateLimitPerMinute: number;
}

export function loadConfig(env: NodeJS.ProcessEnv = process.env): Config {
  const e = EnvSchema.parse(env);
  return {
    port: e.PORT,
    fakeStoreBaseUrl: e.FAKE_STORE_BASE_URL.replace(/\/$/, ''),
    featuredProductIds: e.FEATURED_PRODUCT_IDS.split(',').map((s) => s.trim()).filter(Boolean),
    topRatedPerCategory: e.TOP_RATED_PER_CATEGORY,
    salesInboxEmail: e.SALES_INBOX_EMAIL,
    emailFrom: e.EMAIL_FROM,
    smtp: { host: e.SMTP_HOST, port: e.SMTP_PORT, user: e.SMTP_USER, pass: e.SMTP_PASS },
    enquiryRateLimitPerMinute: e.ENQUIRY_RATE_LIMIT_PER_MINUTE,
  };
}
