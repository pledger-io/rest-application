import {Component, OnInit, TemplateRef} from '@angular/core';
import {ToastService} from "../../core-services";

@Component({
  selector: 'app-toasty',
  templateUrl: './toasty.component.html',
  styleUrls: ['./toasty.component.scss'],
  host: {'[class.ngb-toasts]': 'true'}
})
export class ToastyComponent implements OnInit {

  constructor(public toastService: ToastService) { }

  ngOnInit() {
  }

  isTemplate(toast) { return toast.textOrTpl instanceof TemplateRef; }

}
