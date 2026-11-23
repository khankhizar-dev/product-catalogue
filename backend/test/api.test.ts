import { describe, expect, it } from 'vitest';
import { ProviderError } from '../src/domain/product.js';
import type { ProductProvider } from '../src/providers/ProductProvider.js';
import { makeApp } from './helpers.js';

const valid = { productId: '1', name: 'Ada', email: 'ada@example.com', phone: '+91 98765 43210', message: 'Is it in stock?' };

describe('catalogue API', () => {
  it('GET /api/products/featured returns the featured products', async () => {
    const { app } = await makeApp();
    const res = await app.inject('/api/products/featured');
    expect(res.statusCode).toBe(200);
    expect(res.json().products.map((x: { id: string }) => x.id)).toEqual(['3', '1', '6', '7']);
  });

  it('GET /api/categories/:category/products returns topRated and others', async () => {
    const { app } = await makeApp();
    const res = await app.inject('/api/categories/jewelery/products');
    expect(res.statusCode).toBe(200);
    expect(res.json().topRated).toHaveLength(2);
    expect(res.json().others).toHaveLength(3);
    expect((await app.inject('/api/categories/unknown/products')).statusCode).toBe(404);
  });

  it('GET /api/products?q= returns only matches and rejects a missing query', async () => {
    const { app } = await makeApp();
    const res = await app.inject('/api/products?q=gold');
    expect(res.json().products.map((x: { id: string }) => x.id)).toEqual(['1']);
    expect((await app.inject('/api/products')).statusCode).toBe(400);
    expect((await app.inject('/api/products?q=%20%20')).statusCode).toBe(400);
  });

  it('GET /api/products/:id returns the product or 404', async () => {
    const { app } = await makeApp();
    expect((await app.inject('/api/products/1')).json().product.title).toBe('Gold Ring');
    expect((await app.inject('/api/products/404')).statusCode).toBe(404);
  });

  it('maps a failing data source to 502 without leaking details', async () => {
    const broken: ProductProvider = {
      listProducts: async () => { throw new ProviderError('secret internal detail'); },
      getProduct: async () => null,
      listCategories: async () => [],
    };
    const { app } = await makeApp({ provider: broken });
    const res = await app.inject('/api/products/featured');
    expect(res.statusCode).toBe(502);
    expect(res.body).not.toContain('secret');
  });
});

describe('enquiry API', () => {
  it('sends an email to the sales inbox and returns 201', async () => {
    const { app, email } = await makeApp();
    const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: valid });
    expect(res.statusCode).toBe(201);
    expect(email.sent).toHaveLength(1);
    expect(email.sent[0]).toMatchObject({ to: 'sales@example.com', replyTo: 'ada@example.com' });
    expect(email.sent[0]!.subject).toContain('Gold Ring');
    expect(email.sent[0]!.text).toContain('Is it in stock?');
  });

  it('rejects invalid input with 400 and sends nothing', async () => {
    const { app, email } = await makeApp();
    for (const bad of [{ ...valid, email: 'nope' }, { ...valid, name: '' }, { ...valid, message: 'x'.repeat(2001) }, { ...valid, phone: 'abc' }, {}]) {
      const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: bad });
      expect(res.statusCode).toBe(400);
      expect(res.json().error).toBe('validation_error');
    }
    expect(email.sent).toHaveLength(0);
  });

  it('allows the phone to be omitted or blank', async () => {
    const { app, email } = await makeApp();
    const { phone: _phone, ...noPhone } = valid;
    expect((await app.inject({ method: 'POST', url: '/api/enquiries', payload: noPhone })).statusCode).toBe(201);
    expect((await app.inject({ method: 'POST', url: '/api/enquiries', payload: { ...valid, phone: '' } })).statusCode).toBe(201);
    expect(email.sent[0]!.text).toContain('Phone: not provided');
  });

  it('returns 404 for an unknown product', async () => {
    const { app } = await makeApp();
    const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: { ...valid, productId: '404' } });
    expect(res.statusCode).toBe(404);
  });

  it('escapes user input in the HTML body', async () => {
    const { app, email } = await makeApp();
    await app.inject({ method: 'POST', url: '/api/enquiries', payload: { ...valid, name: '<script>alert(1)</script>' } });
    expect(email.sent[0]!.html).not.toContain('<script>');
    expect(email.sent[0]!.html).toContain('&lt;script&gt;');
  });

  it('returns 502 (not a thank-you) when email delivery fails', async () => {
    const { app, email } = await makeApp();
    email.fail = true;
    const res = await app.inject({ method: 'POST', url: '/api/enquiries', payload: valid });
    expect(res.statusCode).toBe(502);
  });

  it('rate limits the enquiry endpoint', async () => {
    const { app } = await makeApp({ rateLimit: 2 });
    const codes = [];
    for (let i = 0; i < 3; i++) codes.push((await app.inject({ method: 'POST', url: '/api/enquiries', payload: valid })).statusCode);
    expect(codes).toEqual([201, 201, 429]);
  });
});
