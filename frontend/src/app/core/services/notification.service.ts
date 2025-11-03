import { Injectable, signal } from '@angular/core';

export interface NotificationMessage {
  message: string;
  type: 'success' | 'error';
  details?: string;
}

@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  // Signal publico para NotificationComponent
  public notification = signal<NotificationMessage | null>(null);

  // Signal privado para timer
  private timerSignal = signal<any | null>(null);

  /**
   * Exibe uma mensagem de sucesso.
   * @param message A mensagem principal.
   * @param details (Opcional) Detalhes adicionais (ex: erro da API).
   */
  showSuccess(message: string, details?: string): void {
    this.show(message, 'success', details);
  }

  /**
   * Exibe uma mensagem de erro.
   * @param message A mensagem principal.
   * @param details (Opcional) Detalhes adicionais.
   */
  showError(message: string, details?: string): void {
    this.show(message, 'error', details);
  }

  clear(): void {
    this.notification.set(null);
    this.clearTimer();
  }

  private show(message: string, type: 'success' | 'error', details?: string): void {
    this.notification.set({ message, type, details });

    this.clearTimer();

    const timer = setTimeout(() => {
      this.clear();
    }, 5000);

    this.timerSignal.set(timer);
  }

  private clearTimer(): void {
    const timer = this.timerSignal();
    if (timer) {
      clearTimeout(timer);
      this.timerSignal.set(null);
    }
  }
}

