import Fastify, { type FastifyInstance } from 'fastify';
import helmet from '@fastify/helmet';
import rateLimit from '@fastify/rate-limit';
import { ZodError } from 'zod';
import { NotFoundError, ProviderError } from './domain/product.js';
import { EmailDeliveryError } from './email/EmailSender.js';
import { EnquirySchema, type EnquiryService } from './service/EnquiryService.js';
import type { CatalogService } from './service/CatalogService.js';

export interface AppDeps {
  catalog: CatalogService;
  enquiries: EnquiryService;
  enquiryRateLimitPerMinute: number;
  logger?: boolean;
}

export async function buildApp(deps: AppDeps): Promise<FastifyInstance> {
  const app = Fastify({ logger: deps.logger ?? false });
  await app.register(helmet);
  await app.register(rateLimit, { global: false });

  app.setErrorHandler((err, req, reply) => {
    if (err instanceof ZodError) {
      return reply.status(400).send({
        error: 'validation_error',
        details: err.issues.map((i) => ({ field: i.path.join('.'), message: i.message })),
      });
    }
    if (err instanceof NotFoundError) return reply.status(404).send({ error: 'not_found', message: err.message });
    if (err instanceof ProviderError) {
      req.log.error({ err }, 'product source failure');
      return reply.status(502).send({ error: 'product_source_unavailable' });
    }
    if (err instanceof EmailDeliveryError) {
      req.log.error({ err }, 'email delivery failure');
      return reply.status(502).send({ error: 'email_unavailable' });
    }
    const status = (err as { statusCode?: number }).statusCode;
    if (status && status >= 400 && status < 500) {
      return reply.status(status).send({ error: 'bad_request', message: (err as Error).message });
    }
    req.log.error({ err }, 'unhandled error');
    return reply.status(500).send({ error: 'internal_error' });
  });

  app.get('/health', async () => ({ status: 'ok' }));

  app.get('/api/products/featured', async () => ({ products: await deps.catalog.featured() }));

  app.get('/api/products', async (req) => {
    const { q } = req.query as { q?: string };
    if (!q || !q.trim()) {
      throw Object.assign(new Error('Query parameter "q" is required'), { statusCode: 400 });
    }
    if (q.length > 100) throw Object.assign(new Error('Query too long'), { statusCode: 400 });
    return { query: q, products: await deps.catalog.search(q) };
  });

  app.get('/api/products/:id', async (req) => {
    const { id } = req.params as { id: string };
    return { product: await deps.catalog.getProduct(id) };
  });

  app.get('/api/categories', async () => ({ categories: await deps.catalog.categories() }));

  app.get('/api/categories/:category/products', async (req) => {
    const { category } = req.params as { category: string };
    return deps.catalog.byCategory(category);
  });

  app.post(
    '/api/enquiries',
    { config: { rateLimit: { max: deps.enquiryRateLimitPerMinute, timeWindow: '1 minute' } } },
    async (req, reply) => {
      const enquiry = EnquirySchema.parse(req.body);
      await deps.enquiries.submit(enquiry);
      return reply.status(201).send({ status: 'received' });
    },
  );

  return app;
}
