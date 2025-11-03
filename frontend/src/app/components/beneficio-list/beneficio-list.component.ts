import { Component, OnInit, inject, signal, computed } from '@angular/core'; // Importa 'computed'
import { CurrencyPipe } from '@angular/common';
import { Beneficio } from '../../core/models/beneficio.model';
import { BeneficioService, BeneficioTransferDTO } from '../../core/services/beneficio.service';
import { BeneficioFormComponent } from '../beneficio-form/beneficio-form.component';
import { NotificationService } from '../../core/services/notification.service';
import { HttpErrorResponse } from '@angular/common/http';
import { Observable } from 'rxjs';

@Component({
  selector: 'app-beneficio-list',
  standalone: true,
  imports: [CurrencyPipe, BeneficioFormComponent],
  templateUrl: './beneficio-list.component.html',
  styleUrl: './beneficio-list.component.css'
})
export class BeneficioListComponent implements OnInit {
  private beneficioService = inject(BeneficioService);
  private notificationService = inject(NotificationService);

  public beneficios = signal<Beneficio[]>([]);
  public showForm = signal(false);
  public beneficioSelecionado = signal<Beneficio | null>(null);

  public showTransferModal = signal(false);

  public transferDeId = signal<number | null>(null);
  public transferParaId = signal<number | null>(null);
  public transferValor = signal<string | null>(null);

  // Computed signals para buscar os benefícios selecionados
  public transferDeBeneficio = computed(() => {
    const id = this.transferDeId();
    return id ? this.beneficios().find(b => b.id === id) : null;
  });
  public transferParaBeneficio = computed(() => {
    const id = this.transferParaId();
    return id ? this.beneficios().find( b => b.id === id) : null;
  });
  
  ngOnInit(): void {
    this.loadBeneficios();
  }

  loadBeneficios(): void {
    this.beneficioService.getBeneficios().subscribe({
      next: (data: any) => this.beneficios.set(data),
      error: (err: any) => this.handleError('Erro ao carregar benefícios.', err)
    });
  }

  // --- Gerenciamento do Modal (Formulário) ---

  openForm(beneficio?: Beneficio): void {
    this.beneficioSelecionado.set(beneficio || null);
    this.showForm.set(true);
  }

  closeForm(): void {
    this.showForm.set(false);
    this.beneficioSelecionado.set(null);
  }

  // --- Gerenciamento do Modal (Transferência) ---

  openTransferModal(): void {
    this.showTransferModal.set(true);
  }

  closeTransferModal(): void {
    this.showTransferModal.set(false);
    // Limpa o estado do modal de transferência ao fechar
    this.transferDeId.set(null);
    this.transferParaId.set(null);
    this.transferValor.set(null);
  }

  // --- NOVOS Handlers para os inputs do modal ---
  onDeChange(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    this.transferDeId.set(id ? +id : null);
  }

  onParaChange(event: Event): void {
    const id = (event.target as HTMLSelectElement).value;
    this.transferParaId.set(id ? +id : null);
  }

  onTransferValorChange(event: Event): void {
    const value = (event.target as HTMLInputElement).value;
    const sanitizedValue = value.replace(/[^0-9,.]/g, '').replace(',', '.');
    (event.target as HTMLInputElement).value = sanitizedValue;
    this.transferValor.set(sanitizedValue);
  }


  onSave(beneficioPayload: any): void {
    const beneficioEmEdicao = this.beneficioSelecionado();
    let operacao: Observable<Beneficio>;
    let successMsg: string;

    if (beneficioEmEdicao?.id) {
      operacao = this.beneficioService.updateBeneficio(
        beneficioEmEdicao.id,
        beneficioPayload
      );
      successMsg = 'Benefício atualizado com sucesso!';
    } else {
      operacao = this.beneficioService.createBeneficio(beneficioPayload);
      successMsg = 'Benefício criado com sucesso!';
    }

    operacao.subscribe({
      next: () => {
        this.loadBeneficios();
        this.closeForm();
        this.notificationService.showSuccess(successMsg);
      },
      error: (err: any) => this.handleError('Erro ao salvar benefício.', err),
    });
  }

  onDelete(id: number): void {
    if (confirm('Tem certeza que deseja excluir este benefício?')) {
      this.beneficioService.deleteBeneficio(id).subscribe({
        next: () => {
          this.loadBeneficios();
          this.notificationService.showSuccess('Benefício excluído com sucesso!');
        },
        error: (err: any) => this.handleError('Erro ao excluir benefício.', err)
      });
    }
  }

onTransfer(): void {
    const idOrigem = this.transferDeId();
    const idDestino = this.transferParaId();
    if (!idOrigem || !idDestino) {
      this.notificationService.showError('Selecione a origem e o destino.');
      return;
    }
    if (idOrigem === idDestino) {
      this.notificationService.showError('A origem e o destino não podem ser iguais.');
      return;
    }

    const valorStr = this.transferValor();
    if (!valorStr) {
      this.notificationService.showError('Informe um valor para transferir.');
      return;
    }

    const valorNum = parseFloat(valorStr);
    if (isNaN(valorNum) || valorNum <= 0) {
      this.notificationService.showError('O valor deve ser um número positivo.');
      return;
    }

    // Validar Saldo
    const beneficioOrigem = this.transferDeBeneficio();
    if (!beneficioOrigem || valorNum > beneficioOrigem.valor) {
      this.notificationService.showError('Saldo insuficiente na origem.');
      return;
    }

    // Preparar Payload
    const payload: BeneficioTransferDTO = {
      idBeneficioOrigem: idOrigem,
      idBeneficioDestino: idDestino,
      valor: valorNum
    };

    this.beneficioService.transferir(payload).subscribe({
      next: () => {
        this.notificationService.showSuccess('Transferência realizada com sucesso!');
        this.loadBeneficios();
        this.closeTransferModal();
      },
      error: (err: any) => this.handleError('Erro ao realizar transferência.', err)
    });
  }

  private handleError(mensagem: string, error: any): void {
    let detail = 'Consulte o console para mais detalhes.';
    if (error instanceof HttpErrorResponse && error.error?.message) {
      detail = error.error.message;
    }
    this.notificationService.showError(mensagem, detail);
    console.error(error);
  }
}

