import { Component, OnInit, OnDestroy } from '@angular/core';
import { trigger, transition, style, animate } from '@angular/animations';
import { DataService } from '../../services/data/data';
@Component({
  selector: 'app-home',
  templateUrl: './home.html',
  standalone: false,
  styleUrls: ['./home.css'],
  animations: [
    trigger('fadeAnimation', [
      transition(':enter', [
        style({ opacity: 0 }),
        animate('100ms ease-out', style({ opacity: 1 }))
      ])
    ])
  ]
})
export class Home implements OnInit, OnDestroy {
  images = [
    'abeja.jpg',
    'apiario_hero.jpg',
    'frasco.jpg'
  ];
  currentImage = this.images[0];
  private index = 0;
  private intervalId: any;
  constructor(private dataService: DataService) { }

  ngOnInit() {
    this.dataService.clearData()
    this.intervalId = setInterval(() => {
      this.index = (this.index + 1) % this.images.length;
      this.currentImage = this.images[this.index];
    }, 500); 
  }

  ngOnDestroy() {
    if (this.intervalId) {
      clearInterval(this.intervalId);
    }
  }
}
import { from } from 'rxjs';

