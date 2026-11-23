import { z } from 'zod';
import { ProviderError, type Product } from '../domain/product.js';
import type { ProductProvider } from './ProductProvider.js';

const FakeStoreProductSchema = z.object({
  id: z.number(),
  title: z.string(),
  price: z.number(),
  description: z.string(),
  category: z.string(),
  image: z.string(),
  rating: z.object({ rate: z.number(), count: z.number() }).optional(),
});
type FakeStoreProduct = z.infer<typeof FakeStoreProductSchema>;

export function toProduct(p: FakeStoreProduct): Product {
  return {
    id: String(p.id),
    title: p.title,
    description: p.description,
    category: p.category,
    price: p.price,
    imageUrl: p.image,
    rating: p.rating ?? { rate: 0, count: 0 },
  };
}

export class FakeStoreProvider implements ProductProvider {
  constructor(
    private readonly baseUrl: string,
    private readonly fetchFn: typeof fetch = fetch,
    private readonly timeoutMs = 5000,
  ) {}

  async listProducts(): Promise<Product[]> {
    const data = await this.get('/products');
    return this.parse(z.array(FakeStoreProductSchema), data).map(toProduct);
  }

  async getProduct(id: string): Promise<Product | null> {
    // Ids are digits for this source; also keeps the URL path safe.
    if (!/^\d+$/.test(id)) return null;
    const data = await this.get(`/products/${id}`);
    // Fake Store returns an empty body for unknown ids.
    if (data === null) return null;
    return toProduct(this.parse(FakeStoreProductSchema, data));
  }

  async listCategories(): Promise<string[]> {
    const data = await this.get('/products/categories');
    return this.parse(z.array(z.string()), data);
  }

  private parse<T>(schema: z.ZodType<T>, data: unknown): T {
    const result = schema.safeParse(data);
    if (!result.success) {
      throw new ProviderError('Product source returned an unexpected shape', { cause: result.error });
    }
    return result.data;
  }

  private async get(path: string): Promise<unknown> {
    try {
      const res = await this.fetchFn(`${this.baseUrl}${path}`, {
        signal: AbortSignal.timeout(this.timeoutMs),
      });
      if (!res.ok) throw new Error(`upstream responded ${res.status}`);
      const text = await res.text();
      return text ? JSON.parse(text) : null;
    } catch (err) {
      throw new ProviderError(`Product source request failed: ${path}`, { cause: err });
    }
  }
}
