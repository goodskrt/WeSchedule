import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-modal-schedule',
  imports: [CommonModule],
  templateUrl: './modal-schedule.html',
  styleUrl: './modal-schedule.scss',
})
export class ModalSchedule {
  @Input() existingEvent: any = null;
  @Input() selectedTimeSlot: any = null;
  @Output() close = new EventEmitter<void>();
  @Output() eventAdded = new EventEmitter<any>();
  @Output() eventUpdated = new EventEmitter<any>();

  onClose() {
    this.close.emit();
  }
}
