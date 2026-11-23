import { NotFoundError, type Product } from '../domain/product.js';
import type { ProductProvider } from '../providers/ProductProvider.js';

export interface CatalogOptions {
  featuredProductIds: string[];
  topRatedPerCategory: number;
}

export interface CategoryListing {
  category: string;
  topRated: Product[];
  others: Product[];
}

const byRating = (a: Product, b: Product) =>
  b.rating.rate - a.rating.rate || b.rating.count - a.rating.count || a.id.localeCompare(b.id);

export class CatalogService {
  constructor(
    private readonly provider: ProductProvider,
    private readonly opts: CatalogOptions,
  ) {}

  /** The products the entrepreneur chose, in configured order. Unknown ids are skipped. */
  async featured(): Promise<Product[]> {
    const all = await this.provider.listProducts();
    const byId = new Map(all.map((p) => [p.id, p]));
    return this.opts.featuredProductIds.flatMap((id) => byId.get(id) ?? []);
  }

  categories(): Promise<string[]> {
    return this.provider.listCategories();
  }

  async byCategory(category: string): Promise<CategoryListing> {
    const categories = await this.provider.listCategories();
    const match = categories.find((c) => c.toLowerCase() === category.toLowerCase());
    if (!match) throw new NotFoundError(`Category not found: ${category}`);

    const inCategory = (await this.provider.listProducts()).filter((p) => p.category === match);
    const ranked = [...inCategory].sort(byRating);
    const topRated = ranked.slice(0, this.opts.topRatedPerCategory);
    const topIds = new Set(topRated.map((p) => p.id));
    return { category: match, topRated, others: inCategory.filter((p) => !topIds.has(p.id)) };
  }

  /** Case-insensitive match on title, description or category. Every word must match. */
  async search(query: string): Promise<Product[]> {
    const terms = query.toLowerCase().split(/\s+/).filter(Boolean);
    if (terms.length === 0) return [];
    const all = await this.provider.listProducts();
    return all.filter((p) => {
      const haystack = `${p.title} ${p.description} ${p.category}`.toLowerCase();
      return terms.every((t) => haystack.includes(t));
    });
  }

  async getProduct(id: string): Promise<Product> {
    const product = await this.provider.getProduct(id);
    if (!product) throw new NotFoundError(`Product not found: ${id}`);
    return product;
  }
}
