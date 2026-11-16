// bubbleEntities.js
export class Bubble {
    constructor(canvasWidth, canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.reset();
    }

    reset() {
        this.x = Math.random() * (this.canvasWidth || window.innerWidth);
        this.y = (this.canvasHeight || window.innerHeight) + 10;
        this.size = Math.random() * 3.5 + 1.5;
        this.speed = Math.random() * 0.8 + 0.4;
        this.wobble = Math.random() * Math.PI * 2;
    }

    update(canvasWidth, canvasHeight) {
        this.canvasWidth = canvasWidth;
        this.canvasHeight = canvasHeight;
        this.y -= this.speed;
        this.wobble += 0.05;
        this.x += Math.sin(this.wobble) * 0.4;

        if (this.y < -10) {
            this.reset();
        }
    }

    draw(ctx) {
        ctx.save();
        ctx.fillStyle = 'rgba(255, 255, 255, 0.25)';
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.5)';
        ctx.lineWidth = 0.8;

        ctx.beginPath();
        ctx.arc(this.x, this.y, this.size, 0, Math.PI * 2);
        ctx.fill();
        ctx.stroke();

        ctx.restore();
    }
}