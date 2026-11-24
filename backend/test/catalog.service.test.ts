import { describe, expect, it } from 'vitest';
import { NotFoundError } from '../src/domain/product.js';
import { CatalogService } from '../src/service/CatalogService.js';
import { InMemoryProvider, makeApp, p } from './helpers.js';

describe('CatalogService', () => {
  it('returns featured products in configured order and skips unknown ids', async () => {
    const { catalog } = await makeApp();
    expect((await catalog.featured()).map((x) => x.id)).toEqual(['3', '1', '6', '7']);
  });

  it('splits a category into top rated (by rating) then the rest', async () => {
    const { catalog } = await makeApp();
    const r = await catalog.byCategory('jewelery');
    expect(r.topRated.map((x) => x.id)).toEqual(['3', '1']);
    expect(r.others.map((x) => x.id).sort()).toEqual(['2', '4', '5']);
  });

  it('matches categories case-insensitively and rejects unknown ones', async () => {
    const { catalog } = await makeApp();
    expect((await catalog.byCategory('ELECTRONICS')).category).toBe('electronics');
    await expect(catalog.byCategory('nope')).rejects.toBeInstanceOf(NotFoundError);
  });

  it('breaks rating ties by numeric product id', async () => {
    const catalog = new CatalogService(
      new InMemoryProvider([
        p('10', 'jewelery', 4.5, 50),
        p('2', 'jewelery', 4.5, 50),
        p('3', 'jewelery', 4.5, 50),
      ]),
      { featuredProductIds: [], topRatedPerCategory: 3 },
    );

    expect((await catalog.byCategory('jewelery')).topRated.map((product) => product.id)).toEqual(['2', '3', '10']);
  });

  it('search only returns products whose text matches every term', async () => {
    const { catalog } = await makeApp();
    expect((await catalog.search('gold')).map((x) => x.id)).toEqual(['1']);
    expect((await catalog.search('FAST storage')).map((x) => x.id)).toEqual(['6']);
    expect(await catalog.search('gold storage')).toEqual([]);
  });

  it('search also matches the category name', async () => {
    const { catalog } = await makeApp();
    expect((await catalog.search('electronics')).map((x) => x.id)).toEqual(['6', '7']);
  });
});
