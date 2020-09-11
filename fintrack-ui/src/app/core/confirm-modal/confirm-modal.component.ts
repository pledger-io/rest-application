import {AfterViewInit, Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";

@Component({
  selector: 'app-confirm-modal',
  templateUrl: './confirm-modal.component.html',
  styleUrls: ['./confirm-modal.component.scss']
})
export class ConfirmModalComponent implements OnInit, AfterViewInit {

  public titleTextKey: string;
  public descriptionKey: string;

  @ViewChild('okButton')
  private _okButton: ElementRef;

  constructor(public modal: NgbActiveModal) { }

  ngOnInit() {
  }

  ngAfterViewInit(): void {
    this._okButton.nativeElement.focus();
  }

}
