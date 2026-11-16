// foodPelletEntities.js
export class FoodPellet {
    constructor(x, y) {
        this.x = x;
        this.y = y;
        this.size = 3.5;
        this.vy = 0.6 + Math.random() * 0.4;
        this.vx = (Math.random() - 0.5) * 0.3;
        this.opacity = 1.0;
    }

    update(canvasHeight) {
        this.x += this.vx;
        this.y += this.vy;

        const floorY = (canvasHeight || window.innerHeight) - 45;
        if (this.y >= floorY - 5) {
            this.y = floorY - 5;
            this.opacity -= 0.015;
        }
    }

    draw(ctx) {
        ctx.save();
        ctx.globalAlpha = Math.max(0, this.opacity);

        ctx.fillStyle = '#8b4513';
        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#d2691e';
        ctx.beginPath();
        ctx.arc(this.x - 1, this.y - 1, this.size - 0.5, 0, Math.PI * 2);
        ctx.fill();

        ctx.restore();
    }

    isExpired() {
        return this.opacity <= 0;
    }
}