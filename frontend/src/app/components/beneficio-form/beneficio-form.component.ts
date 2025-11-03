import {
  Component,
  Input,
  Output,
  EventEmitter,
  OnChanges,
  inject,
  SimpleChanges,
  signal,
} from '@angular/core';
import { CommonModule } from '@angular/common';
import {
  FormBuilder,
  FormGroup,
  ReactiveFormsModule,
  Validators,
} from '@angular/forms';
import { Beneficio } from '../../core/models/beneficio.model';

@Component({
  selector: 'app-beneficio-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './beneficio-form.component.html',
  styleUrl: './beneficio-form.component.css',
})
export class BeneficioFormComponent implements OnChanges {
  // --- Comunicação com o Pai (Smart Component) ---
  @Input() beneficio: Beneficio | null = null;
  @Output() save = new EventEmitter<any>();
  @Output() close = new EventEmitter<void>();

  private fb = inject(FormBuilder);
  public beneficioForm: FormGroup;
  public submitting = signal(false);

  constructor() {
    // Define a estrutura e validações do formulário
    this.beneficioForm = this.fb.group({
      nome: ['', [Validators.required, Validators.minLength(3)]],
      descricao: [''],
      valor: [null, [Validators.required, Validators.min(0.01)]],
      ativo: [true],
    });
  }

  /**
   * Detecta quando o @Input() 'beneficio' muda.
   * Se 'beneficio' for nulo (modo Criação), reseta o form.
   * Se 'beneficio' existir (modo Edição), preenche o form.
   */
  ngOnChanges(changes: SimpleChanges): void {
    if (changes['beneficio']) {
      if (this.beneficio) {
        // Modo Edição: Preenche o formulário
        this.beneficioForm.patchValue(this.beneficio);
      } else {
        // Modo Criação: Reseta
        this.beneficioForm.reset({ ativo: true, valor: null });
      }
    }
  }

  // --- Getters para Validação (facilitam o HTML) ---
  get nome() {
    return this.beneficioForm.get('nome');
  }
  get valor() {
    return this.beneficioForm.get('valor');
  }

  // --- Ações ---

  onSubmit(): void {
    if (this.beneficioForm.invalid) {
      this.beneficioForm.markAllAsTouched(); // Mostra erros
      return;
    }

    this.submitting.set(true);
    // Emite o evento (save) para o componente pai (BeneficioList)
    // O componente pai é quem de fato chama o serviço.
    this.save.emit(this.beneficioForm.value);

    // O pai é responsável por fechar o modal,
    // mas resetamos o estado de submitting.
    // (Poderia ser controlado pelo pai também)
    setTimeout(() => this.submitting.set(false), 1000);
  }

  onClose(): void {
    // Emite o evento (close) para o componente pai.
    this.close.emit();
  }
}
