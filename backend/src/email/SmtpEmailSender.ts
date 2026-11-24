import nodemailer, { type Transporter } from 'nodemailer';
import { EmailDeliveryError, type EmailMessage, type EmailSender } from './EmailSender.js';

export interface SmtpOptions {
  host: string;
  port: number;
  user?: string;
  pass?: string;
  from: string;
}

export class SmtpEmailSender implements EmailSender {
  private readonly transporter: Transporter;

  constructor(private readonly opts: SmtpOptions) {
    this.transporter = nodemailer.createTransport({
      host: opts.host,
      port: opts.port,
      secure: opts.port === 465,
      auth: opts.user ? { user: opts.user, pass: opts.pass } : undefined,
    });
  }

  async send(message: EmailMessage): Promise<void> {
    try {
      await this.transporter.sendMail({ from: this.opts.from, ...message });
    } catch (err) {
      throw new EmailDeliveryError('Failed to send email', { cause: err });
    }
  }
}
