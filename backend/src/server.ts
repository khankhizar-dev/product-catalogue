import 'dotenv/config'; // loads .env for local runs; real env vars (e.g. from Docker) take precedence
import { buildApp } from './app.js';
import { loadConfig } from './config.js';
import { SmtpEmailSender } from './email/SmtpEmailSender.js';
import { FakeStoreProvider } from './providers/FakeStoreProvider.js';
import { CatalogService } from './service/CatalogService.js';
import { EnquiryService } from './service/EnquiryService.js';

// Composition root: the only place concrete implementations are chosen.
const config = loadConfig();
const provider = new FakeStoreProvider(config.fakeStoreBaseUrl);
const email = new SmtpEmailSender({ ...config.smtp, from: config.emailFrom });
const catalog = new CatalogService(provider, {
  featuredProductIds: config.featuredProductIds,
  topRatedPerCategory: config.topRatedPerCategory,
});
const enquiries = new EnquiryService(catalog, email, config.salesInboxEmail);

const app = await buildApp({
  catalog,
  enquiries,
  enquiryRateLimitPerMinute: config.enquiryRateLimitPerMinute,
  logger: true,
});
await app.listen({ port: config.port, host: '0.0.0.0' });
