import { Component } from '@angular/core';
import { DesignerPageComponent } from './features/designer/designer-page.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [DesignerPageComponent],
  templateUrl: './app.component.html',
})
export class AppComponent {}
