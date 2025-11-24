import { Injectable } from '@angular/core';
import { Howl, Howler } from 'howler';

@Injectable({
  providedIn: 'root'
})
export class AudioService {
  private sounds: Map<string, Howl> = new Map();

  play(url: string, volume: number = 1.0): void {
    try {
      let sound = this.sounds.get(url);

      if (!sound) {
        sound = new Howl({
          src: [url],
          volume,
          html5: true // fuerza el uso del audio nativo para compatibilidad
        });
        this.sounds.set(url, sound);
      }

      sound.play();
    } catch (err) {
      console.error('Error al reproducir sonido:', err);
    }
  }

  stop(url?: string): void {
    if (url && this.sounds.has(url)) {
      this.sounds.get(url)?.stop();
    } else {
      Howler.stop();
    }
  }

  setVolume(volume: number): void {
    Howler.volume(volume);
  }
}
