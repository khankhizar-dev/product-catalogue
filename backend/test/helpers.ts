import type { Product } from '../src/domain/product.js';
import type { EmailMessage, EmailSender } from '../src/email/EmailSender.js';
import { EmailDeliveryError } from '../src/email/EmailSender.js';
import type { ProductProvider } from '../src/providers/ProductProvider.js';
import { buildApp } from '../src/app.js';
import { CatalogService } from '../src/service/CatalogService.js';
import { EnquiryService } from '../src/service/EnquiryService.js';

export const p = (id: string, category: string, rate: number, count = 10, extra: Partial<Product> = {}): Product => ({
  id,
  title: `Product ${id}`,
  description: `Description ${id}`,
  category,
  price: 10,
  imageUrl: `http://img/${id}.png`,
  rating: { rate, count },
  ...extra,
});

export const sampleProducts: Product[] = [
  p('1', 'jewelery', 4.5, 100, { title: 'Gold Ring' }),
  p('2', 'jewelery', 3.0),
  p('3', 'jewelery', 4.9),
  p('4', 'jewelery', 2.0),
  p('5', 'jewelery', 4.0),
  p('6', 'electronics', 4.2, 50, { title: 'USB Drive', description: 'Fast storage' }),
  p('7', 'electronics', 3.8),
];

export class InMemoryProvider implements ProductProvider {
  constructor(private products: Product[] = sampleProducts) {}
  async listProducts() { return this.products; }
  async getProduct(id: string) { return this.products.find((x) => x.id === id) ?? null; }
  async listCategories() { return [...new Set(this.products.map((x) => x.category))]; }
}

export class RecordingEmailSender implements EmailSender {
  sent: EmailMessage[] = [];
  fail = false;
  async send(m: EmailMessage) {
    if (this.fail) throw new EmailDeliveryError('boom');
    this.sent.push(m);
  }
}

export async function makeApp(opts: { rateLimit?: number; provider?: ProductProvider } = {}) {
  const email = new RecordingEmailSender();
  const catalog = new CatalogService(opts.provider ?? new InMemoryProvider(), {
    featuredProductIds: ['3', '1', '6', '999', '7'],
    topRatedPerCategory: 2,
  });
  const enquiries = new EnquiryService(catalog, email, 'sales@example.com');
  const app = await buildApp({ catalog, enquiries, enquiryRateLimitPerMinute: opts.rateLimit ?? 100 });
  return { app, email, catalog };
}
