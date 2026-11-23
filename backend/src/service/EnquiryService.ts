import { z } from 'zod';
import type { EmailSender } from '../email/EmailSender.js';
import type { CatalogService } from './CatalogService.js';

export const EnquirySchema = z.object({
  productId: z.string().trim().min(1).max(50),
  name: z.string().trim().min(1).max(100),
  email: z.string().trim().email().max(254),
  phone: z
    .string()
    .trim()
    .regex(/^[0-9+()\-\s]{5,20}$/, 'Invalid phone number')
    .optional()
    .or(z.literal('').transform(() => undefined)),
  message: z.string().trim().min(1).max(2000),
});
export type Enquiry = z.infer<typeof EnquirySchema>;

export const escapeHtml = (s: string) =>
  s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;').replace(/'/g, '&#39;');

/** Strip line breaks so user text can never inject mail headers via the subject. */
const oneLine = (s: string) => s.replace(/[\r\n]+/g, ' ').trim();

export class EnquiryService {
  constructor(
    private readonly catalog: CatalogService,
    private readonly email: EmailSender,
    private readonly salesInbox: string,
  ) {}

  async submit(enquiry: Enquiry): Promise<void> {
    const product = await this.catalog.getProduct(enquiry.productId);
    const phone = enquiry.phone ?? 'not provided';

    await this.email.send({
      to: this.salesInbox,
      replyTo: enquiry.email,
      subject: `New enquiry: ${oneLine(product.title)}`,
      text: [
        `Product: ${product.title} (id ${product.id})`,
        `Name: ${enquiry.name}`,
        `Email: ${enquiry.email}`,
        `Phone: ${phone}`,
        '',
        enquiry.message,
      ].join('\n'),
      html: [
        `<p><strong>Product:</strong> ${escapeHtml(product.title)} (id ${escapeHtml(product.id)})</p>`,
        `<p><strong>Name:</strong> ${escapeHtml(enquiry.name)}<br>`,
        `<strong>Email:</strong> ${escapeHtml(enquiry.email)}<br>`,
        `<strong>Phone:</strong> ${escapeHtml(phone)}</p>`,
        `<p>${escapeHtml(enquiry.message).replace(/\n/g, '<br>')}</p>`,
      ].join('\n'),
    });
  }
}
