// fishEntities.js - Detailed 2D Fish data and realistic swimming class

export class DetailedFish {
    constructor(typeConfig, canvasWidth, canvasHeight) {
        this.type = typeConfig.type || 'clownfish';
        this.name = typeConfig.name || 'Fish';
        this.x = Math.random() * ((canvasWidth || window.innerWidth) - 200) + 100;
        this.y = Math.random() * ((canvasHeight || window.innerHeight) - 250) + 100;
        this.size = typeConfig.size || 25;
        this.color1 = typeConfig.color1 || '#ff4500';
        this.color2 = typeConfig.color2 || '#ffffff';
        this.maxSpeed = typeConfig.maxSpeed || 1.4;

        this.vx = (Math.random() - 0.5) * this.maxSpeed;
        this.vy = (Math.random() - 0.5) * (this.maxSpeed * 0.5);
        this.ax = 0;
        this.ay = 0;

        this.targetFood = null;
        this.tailWag = Math.random() * Math.PI * 2;
        this.finWag = Math.random() * Math.PI * 2;
        this.facingRight = this.vx >= 0;
    }

    update(canvasWidth, canvasHeight, foods) {
        if (foods && foods.length > 0) {
            this.findNearestFood(foods);
        } else {
            this.targetFood = null;
        }

        if (this.targetFood) {
            const dx = this.targetFood.x - this.x;
            const dy = this.targetFood.y - this.y;
            const dist = Math.sqrt(dx * dx + dy * dy);

            if (dist > 5) {
                this.ax = (dx / dist) * 0.12;
                this.ay = (dy / dist) * 0.12;
            }
        } else {
            if (Math.random() < 0.03) {
                this.ax = (Math.random() - 0.5) * 0.1;
                this.ay = (Math.random() - 0.5) * 0.08;
            }
        }

        this.vx += this.ax;
        this.vy += this.ay;

        const speed = Math.sqrt(this.vx * this.vx + this.vy * this.vy);
        const currentMax = this.targetFood ? this.maxSpeed * 1.5 : this.maxSpeed;
        if (speed > currentMax) {
            this.vx = (this.vx / speed) * currentMax;
            this.vy = (this.vy / speed) * currentMax;
        }

        this.x += this.vx;
        this.y += this.vy;

        if (Math.abs(this.vx) > 0.1) {
            this.facingRight = this.vx > 0;
        }

        const marginX = 80;
        const marginY = 80;
        const floorHeight = 45;
        const currentCanvasHeight = canvasHeight || window.innerHeight;
        const currentCanvasWidth = canvasWidth || window.innerWidth;
        const floorY = currentCanvasHeight - floorHeight;

        if (this.x < marginX) this.vx += 0.08;
        if (this.x > currentCanvasWidth - marginX) this.vx -= 0.08;
        if (this.y < marginY) this.vy += 0.08;

        // Strict boundary clamp to prevent fish from going below the soil
        if (this.y > floorY - this.size) {
            this.y = floorY - this.size;
            this.vy = -Math.abs(this.vy) * 0.5; // gentle upward bounce
        }

        this.tailWag += 0.1 + Math.abs(speed) * 0.05;
        this.finWag += 0.08;
    }

    findNearestFood(foods) {
        let nearestDist = Infinity;
        let nearest = null;

        foods.forEach(food => {
            const dx = food.x - this.x;
            const dy = food.y - this.y;
            const dist = Math.sqrt(dx * dx + dy * dy);

            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = food;
            }
        });

        this.targetFood = nearest;
    }

    draw(ctx) {
        ctx.save();
        ctx.translate(this.x, this.y);

        if (!this.facingRight) {
            ctx.scale(-1, 1);
        }

        const wag = Math.sin(this.tailWag) * 6;
        const finFlap = Math.sin(this.finWag) * 4;

        if (this.type === 'angel') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.ellipse(0, 0, this.size, this.size * 0.7, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.strokeStyle = this.color2;
            ctx.lineWidth = 3.5;
            ctx.beginPath();
            ctx.moveTo(-this.size * 0.2, -this.size * 0.65);
            ctx.lineTo(-this.size * 0.2, this.size * 0.65);
            ctx.moveTo(this.size * 0.2, -this.size * 0.55);
            ctx.lineTo(this.size * 0.2, this.size * 0.55);
            ctx.stroke();

            ctx.fillStyle = this.color2;
            ctx.beginPath();
            ctx.moveTo(0, -this.size * 0.6);
            ctx.quadraticCurveTo(-this.size * 0.4, -this.size * 1.5, -this.size * 0.9, -this.size * 1.2);
            ctx.lineTo(-this.size * 0.5, -this.size * 0.4);
            ctx.fill();

            ctx.beginPath();
            ctx.moveTo(0, this.size * 0.6);
            ctx.quadraticCurveTo(-this.size * 0.4, this.size * 1.5, -this.size * 0.9, this.size * 1.2);
            ctx.lineTo(-this.size * 0.5, this.size * 0.4);
            ctx.fill();

            ctx.beginPath();
            ctx.moveTo(-this.size, 0);
            ctx.lineTo(-this.size - 16, -14 + wag);
            ctx.lineTo(-this.size - 20, 0);
            ctx.lineTo(-this.size - 16, 14 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'shark') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.moveTo(this.size * 1.1, 0);
            ctx.quadraticCurveTo(0, -this.size * 0.5, -this.size, -3);
            ctx.quadraticCurveTo(0, this.size * 0.5, this.size * 1.1, 0);
            ctx.fill();

            ctx.beginPath();
            ctx.ellipse(this.size * 0.9, 0, 4, this.size * 0.45, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = this.color2;
            ctx.beginPath();
            ctx.moveTo(0, -this.size * 0.3);
            ctx.lineTo(-8, -this.size * 1.0);
            ctx.lineTo(-16, -this.size * 0.2);
            ctx.fill();

            ctx.beginPath();
            ctx.moveTo(-this.size, 0);
            ctx.lineTo(-this.size - 22, -18 + wag);
            ctx.lineTo(-this.size - 14, 0);
            ctx.lineTo(-this.size - 22, 18 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'puffer') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.arc(0, 0, this.size * 0.75, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = this.color2;
            ctx.beginPath();
            ctx.arc(-this.size * 0.3, -this.size * 0.25, 3, 0, Math.PI * 2);
            ctx.arc(0, -this.size * 0.4, 3, 0, Math.PI * 2);
            ctx.arc(this.size * 0.2, this.size * 0.2, 3, 0, Math.PI * 2);
            ctx.arc(-this.size * 0.2, this.size * 0.3, 2.5, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.ellipse(-2, 2, 6, 4, finFlap * 0.1, 0, Math.PI * 2);
            ctx.fill();

            ctx.beginPath();
            ctx.moveTo(-this.size * 0.7, 0);
            ctx.lineTo(-this.size - 10, -8 + wag);
            ctx.lineTo(-this.size - 10, 8 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'clownfish') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.ellipse(0, 0, this.size, this.size * 0.65, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = '#ffffff';
            ctx.strokeStyle = '#000000';
            ctx.lineWidth = 1;

            ctx.beginPath();
            ctx.rect(-3, -this.size * 0.63, 6, this.size * 1.26);
            ctx.fill();
            ctx.stroke();

            ctx.beginPath();
            ctx.rect(-this.size * 0.55, -this.size * 0.5, 5, this.size);
            ctx.fill();
            ctx.stroke();

            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.moveTo(-this.size, 0);
            ctx.lineTo(-this.size - 12, -10 + wag);
            ctx.lineTo(-this.size - 12, 10 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'tang') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.ellipse(0, 0, this.size, this.size * 0.75, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.strokeStyle = '#0a0a23';
            ctx.lineWidth = 4;
            ctx.beginPath();
            ctx.arc(0, 0, this.size * 0.55, 0.2, Math.PI - 0.2);
            ctx.stroke();

            ctx.fillStyle = this.color2;
            ctx.beginPath();
            ctx.moveTo(-this.size, 0);
            ctx.lineTo(-this.size - 14, -12 + wag);
            ctx.lineTo(-this.size - 14, 12 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'koi') {
            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.ellipse(0, 0, this.size * 1.15, this.size * 0.5, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = this.color2;
            ctx.beginPath();
            ctx.ellipse(-this.size * 0.2, -2, this.size * 0.5, this.size * 0.35, 0.1, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.beginPath();
            ctx.moveTo(-this.size * 1.15, 0);
            ctx.lineTo(-this.size * 1.6, -15 + wag);
            ctx.lineTo(-this.size * 1.3, 0);
            ctx.lineTo(-this.size * 1.6, 15 + wag);
            ctx.closePath();
            ctx.fill();

        } else if (this.type === 'idol') {
            ctx.fillStyle = '#ffffff';
            ctx.beginPath();
            ctx.moveTo(this.size * 0.5, -this.size * 0.4);
            ctx.lineTo(-this.size * 0.8, -this.size * 0.6);
            ctx.lineTo(-this.size * 0.8, this.size * 0.6);
            ctx.lineTo(this.size * 0.5, this.size * 0.4);
            ctx.closePath();
            ctx.fill();

            ctx.fillStyle = this.color1;
            ctx.beginPath();
            ctx.moveTo(-this.size * 0.2, -this.size * 0.55);
            ctx.lineTo(-this.size * 0.5, -this.size * 0.58);
            ctx.lineTo(-this.size * 0.5, this.size * 0.58);
            ctx.lineTo(-this.size * 0.2, this.size * 0.55);
            ctx.closePath();
            ctx.fill();

            ctx.fillStyle = this.color2;
            ctx.fillRect(-this.size * 0.2, -this.size * 0.45, this.size * 0.65, this.size * 0.9);

            ctx.strokeStyle = '#111111';
            ctx.lineWidth = 2.5;
            ctx.beginPath();
            ctx.moveTo(-this.size * 0.4, -this.size * 0.58);
            ctx.quadraticCurveTo(-this.size * 0.8, -this.size * 1.6, -this.size * 1.3, -this.size * 2.0);
            ctx.stroke();
        }

        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(this.size * 0.38, -this.size * 0.15, 4.5, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#1e293b';
        ctx.beginPath();
        ctx.arc(this.size * 0.42, -this.size * 0.15, 2.2, 0, Math.PI * 2);
        ctx.fill();

        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(this.size * 0.44, -this.size * 0.18, 0.9, 0, Math.PI * 2);
        ctx.fill();

        ctx.restore();
    }
}