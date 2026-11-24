/** Our own product shape. External sources are mapped to this at the provider boundary. */
export interface Product {
  id: string;
  title: string;
  description: string;
  category: string;
  price: number;
  imageUrl: string;
  rating: { rate: number; count: number };
}

export class NotFoundError extends Error {
  constructor(message: string) {
    super(message);
    this.name = 'NotFoundError';
  }
}

/** The upstream data source failed or returned something unusable. */
export class ProviderError extends Error {
  constructor(message: string, options?: { cause?: unknown }) {
    super(message, options);
    this.name = 'ProviderError';
  }
}
