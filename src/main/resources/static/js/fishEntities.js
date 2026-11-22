export class DetailedFish {
    constructor(typeConfig, canvasWidth, canvasHeight) {
        this.type = typeConfig.type || 'clownfish';
        this.name = typeConfig.name || 'Fish';
        this.x = Math.random() * ((canvasWidth || window.innerWidth) - 200) + 100;
        this.y = Math.random() * ((canvasHeight || window.innerHeight) - 250) + 100;
        this.size = typeConfig.size || 25;
        this.color1 = typeConfig.color1 || '#ff5722';
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
            this.vy = -Math.abs(this.vy) * 0.5;
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

    drawFinRays(ctx, x1, y1, x2, y2, count = 4, alpha = 0.28) {
        ctx.save();
        ctx.strokeStyle = `rgba(15, 23, 42, ${alpha})`;
        ctx.lineWidth = 1;
        for (let i = 1; i <= count; i++) {
            const t = i / (count + 1);
            const px = x1 + (x2 - x1) * t;
            const py = y1 + (y2 - y1) * t;
            ctx.beginPath();
            ctx.moveTo(x1, y1);
            ctx.lineTo(px, py);
            ctx.stroke();
        }
        ctx.restore();
    }

    drawGillArc(ctx, x, radius, startAngle, endAngle, alpha = 0.35) {
        ctx.save();
        ctx.strokeStyle = `rgba(15, 23, 42, ${alpha})`;
        ctx.lineWidth = 1.4;
        ctx.beginPath();
        ctx.arc(x, 0, radius, startAngle, endAngle);
        ctx.stroke();
        ctx.restore();
    }

    drawRealisticEye(ctx, ex, ey, r = 4.6) {
        ctx.save();
        // Outer sclera
        ctx.fillStyle = '#f8fafc';
        ctx.beginPath();
        ctx.arc(ex, ey, r, 0, Math.PI * 2);
        ctx.fill();

        // Warm iris ring
        ctx.fillStyle = '#f59e0b';
        ctx.beginPath();
        ctx.arc(ex + r * 0.12, ey, r * 0.68, 0, Math.PI * 2);
        ctx.fill();

        // Deep pupil
        ctx.fillStyle = '#090d16';
        ctx.beginPath();
        ctx.arc(ex + r * 0.16, ey, r * 0.44, 0, Math.PI * 2);
        ctx.fill();

        // Specular catchlight
        ctx.fillStyle = '#ffffff';
        ctx.beginPath();
        ctx.arc(ex + r * 0.32, ey - r * 0.25, r * 0.22, 0, Math.PI * 2);
        ctx.fill();
        ctx.restore();
    }

    draw(ctx) {
        ctx.save();
        ctx.translate(this.x, this.y);

        if (!this.facingRight) {
            ctx.scale(-1, 1);
        }

        const wag = Math.sin(this.tailWag) * 6;
        const finFlap = Math.sin(this.finWag) * 4;
        const s = this.size;

        if (this.type === 'angel') {
            const bodyColor = this.color1 || '#fbbf24';
            const accentColor = this.color2 || '#fef08a';
            const finSway = finFlap * 0.45;

            // Compact, animated forked tail fin
            const tailGrad = ctx.createLinearGradient(-s * 0.78, 0, -s * 1.32, wag);
            tailGrad.addColorStop(0, accentColor);
            tailGrad.addColorStop(1, 'rgba(255, 255, 255, 0.78)');
            ctx.fillStyle = tailGrad;
            ctx.beginPath();
            ctx.moveTo(-s * 0.78, 0);
            ctx.quadraticCurveTo(-s * 1.25, -s * 0.46 + wag * 0.75, -s * 1.15, -s * 0.14 + wag * 0.5);
            ctx.lineTo(-s * 0.96, wag * 0.4);
            ctx.lineTo(-s * 1.15, s * 0.14 + wag * 0.5);
            ctx.quadraticCurveTo(-s * 1.25, s * 0.46 + wag * 0.75, -s * 0.78, 0);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s * 0.8, 0, -s * 1.2, -s * 0.25 + wag * 0.6, 3, 0.22);
            this.drawFinRays(ctx, -s * 0.8, 0, -s * 1.2, s * 0.25 + wag * 0.6, 3, 0.22);

            // Subtle, gracefully sweeping dorsal fin (keeps height compact)
            ctx.fillStyle = accentColor;
            ctx.beginPath();
            ctx.moveTo(s * 0.18, -s * 0.44);
            ctx.quadraticCurveTo(-s * 0.22, -s * 0.92 + finSway, -s * 0.78, -s * 0.52 + finSway * 0.7);
            ctx.quadraticCurveTo(-s * 0.42, -s * 0.34, -s * 0.25, -s * 0.32);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, s * 0.05, -s * 0.42, -s * 0.65, -s * 0.54 + finSway * 0.7, 3, 0.2);

            // Subtle, gracefully sweeping bottom (anal) fin (stays well above the sand)
            ctx.fillStyle = accentColor;
            ctx.beginPath();
            ctx.moveTo(s * 0.18, s * 0.44);
            ctx.quadraticCurveTo(-s * 0.22, s * 0.92 - finSway, -s * 0.78, s * 0.52 - finSway * 0.7);
            ctx.quadraticCurveTo(-s * 0.42, s * 0.34, -s * 0.25, s * 0.32);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, s * 0.05, s * 0.42, -s * 0.65, s * 0.54 - finSway * 0.7, 3, 0.2);

            // Sleek disc-like angelfish body with shaded gradient
            const bodyGrad = ctx.createLinearGradient(0, -s * 0.56, 0, s * 0.56);
            bodyGrad.addColorStop(0, '#fef08a');
            bodyGrad.addColorStop(0.45, bodyColor);
            bodyGrad.addColorStop(1, '#d97706');
            ctx.fillStyle = bodyGrad;
            ctx.beginPath();
            ctx.moveTo(s * 0.88, 0);
            ctx.quadraticCurveTo(s * 0.15, -s * 0.58, -s * 0.8, 0);
            ctx.quadraticCurveTo(s * 0.15, s * 0.58, s * 0.88, 0);
            ctx.closePath();
            ctx.fill();

            // Classic angelfish vertical stripes
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.85)';
            ctx.lineWidth = 2.6;
            ctx.beginPath();
            ctx.moveTo(-s * 0.22, -s * 0.44);
            ctx.quadraticCurveTo(-s * 0.14, 0, -s * 0.22, s * 0.44);
            ctx.moveTo(s * 0.2, -s * 0.42);
            ctx.quadraticCurveTo(s * 0.28, 0, s * 0.2, s * 0.42);
            ctx.stroke();

            this.drawGillArc(ctx, s * 0.44, s * 0.24, -Math.PI * 0.35, Math.PI * 0.35);

            // Delicate fluttering pectoral fin
            ctx.fillStyle = 'rgba(255, 255, 255, 0.8)';
            ctx.beginPath();
            ctx.ellipse(s * 0.12, s * 0.14, 6, 2.8, finFlap * 0.08 - 0.15, 0, Math.PI * 2);
            ctx.fill();

            this.drawRealisticEye(ctx, s * 0.54, -s * 0.12, 3.9);

        } else if (this.type === 'shark') {
            // Kept your shark silhouette with richer shading & gill slits
            const sharkGrad = ctx.createLinearGradient(0, -s * 0.55, 0, s * 0.55);
            sharkGrad.addColorStop(0, '#cbd5e1');
            sharkGrad.addColorStop(0.55, this.color1 || '#94a3b8');
            sharkGrad.addColorStop(1, '#f8fafc');

            ctx.fillStyle = sharkGrad;
            ctx.beginPath();
            ctx.moveTo(s * 1.1, 0);
            ctx.quadraticCurveTo(0, -s * 0.5, -s, -3);
            ctx.quadraticCurveTo(0, s * 0.5, s * 1.1, 0);
            ctx.fill();

            ctx.beginPath();
            ctx.ellipse(s * 0.9, 0, 4, s * 0.45, 0, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = this.color2 || '#cbd5e1';
            ctx.beginPath();
            ctx.moveTo(0, -s * 0.3);
            ctx.lineTo(-8, -s * 1.0);
            ctx.lineTo(-16, -s * 0.2);
            ctx.fill();
            this.drawFinRays(ctx, -6, -s * 0.25, -9, -s * 0.85, 3, 0.25);

            ctx.beginPath();
            ctx.moveTo(-s, 0);
            ctx.lineTo(-s - 22, -18 + wag);
            ctx.lineTo(-s - 14, 0);
            ctx.lineTo(-s - 22, 18 + wag);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s - 2, 0, -s - 20, -14 + wag, 3, 0.25);
            this.drawFinRays(ctx, -s - 2, 0, -s - 20, 14 + wag, 3, 0.25);

            // Gill slits & subtle jaw line
            ctx.strokeStyle = 'rgba(15, 23, 42, 0.45)';
            ctx.lineWidth = 1.3;
            for (let g = 0; g < 3; g++) {
                const gx = s * (0.28 - g * 0.09);
                ctx.beginPath();
                ctx.moveTo(gx, -s * 0.14);
                ctx.lineTo(gx - 2, s * 0.14);
                ctx.stroke();
            }

            this.drawRealisticEye(ctx, s * 0.55, -s * 0.12, 3.8);

        } else if (this.type === 'puffer') {
            const bodyColor = this.color1 || '#facc15';
            const bellyColor = this.color2 || '#fef9c3';

            // Animated tail fin with rays
            ctx.fillStyle = bellyColor;
            ctx.beginPath();
            ctx.moveTo(-s * 0.72, 0);
            ctx.quadraticCurveTo(-s * 1.28, -s * 0.56 + wag, -s * 1.18, wag * 0.45);
            ctx.quadraticCurveTo(-s * 1.28, s * 0.56 + wag, -s * 0.72, 0);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s * 0.75, 0, -s * 1.18, -s * 0.35 + wag, 3, 0.25);
            this.drawFinRays(ctx, -s * 0.75, 0, -s * 1.18, s * 0.35 + wag, 3, 0.25);

            // Round puffer body with radial highlight
            const puffGrad = ctx.createRadialGradient(-s * 0.15, -s * 0.25, s * 0.1, 0, 0, s * 0.85);
            puffGrad.addColorStop(0, '#fef08a');
            puffGrad.addColorStop(0.55, bodyColor);
            puffGrad.addColorStop(1, '#ca8a04');
            ctx.fillStyle = puffGrad;
            ctx.beginPath();
            ctx.arc(0, 0, s * 0.78, 0, Math.PI * 2);
            ctx.fill();

            // Soft belly
            ctx.fillStyle = bellyColor;
            ctx.beginPath();
            ctx.ellipse(s * 0.08, s * 0.26, s * 0.52, s * 0.36, 0, 0, Math.PI * 2);
            ctx.fill();

            // Tiny protective spines & bright spots
            ctx.strokeStyle = 'rgba(120, 53, 15, 0.4)';
            ctx.lineWidth = 1.2;
            const spineAngles = [-2.5, -2.0, -1.5, -1.0, -0.5, 0.5, 1.0, 1.5, 2.0, 2.5];
            spineAngles.forEach(ang => {
                const sx = Math.cos(ang) * s * 0.76;
                const sy = Math.sin(ang) * s * 0.76;
                ctx.beginPath();
                ctx.moveTo(sx, sy);
                ctx.lineTo(sx + Math.cos(ang) * 4, sy + Math.sin(ang) * 4);
                ctx.stroke();
            });

            ctx.fillStyle = '#ffffff';
            ctx.beginPath();
            ctx.arc(-s * 0.32, -s * 0.22, 2.6, 0, Math.PI * 2);
            ctx.arc(0, -s * 0.42, 2.6, 0, Math.PI * 2);
            ctx.arc(-s * 0.2, s * 0.3, 2.2, 0, Math.PI * 2);
            ctx.fill();

            // Fluttering pectoral fin
            ctx.fillStyle = bellyColor;
            ctx.beginPath();
            ctx.ellipse(s * 0.02, s * 0.14, 6.5, 4, finFlap * 0.12, 0, Math.PI * 2);
            ctx.fill();

            this.drawRealisticEye(ctx, s * 0.42, -s * 0.15, 4.4);

        } else if (this.type === 'clownfish') {
            const bodyColor = this.color1 || '#ff5722';
            const stripeColor = this.color2 || '#ffffff';

            // Rounded tail fin with dark margin & rays
            ctx.fillStyle = bodyColor;
            ctx.strokeStyle = '#1e293b';
            ctx.lineWidth = 1.4;
            ctx.beginPath();
            ctx.moveTo(-s * 0.92, 0);
            ctx.quadraticCurveTo(-s * 1.52, -s * 0.72 + wag, -s * 1.32, wag * 0.5);
            ctx.quadraticCurveTo(-s * 1.52, s * 0.72 + wag, -s * 0.92, 0);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();
            this.drawFinRays(ctx, -s * 0.95, 0, -s * 1.35, -s * 0.42 + wag, 3, 0.28);
            this.drawFinRays(ctx, -s * 0.95, 0, -s * 1.35, s * 0.42 + wag, 3, 0.28);

            // Dorsal fin ridge
            ctx.fillStyle = bodyColor;
            ctx.beginPath();
            ctx.moveTo(-s * 0.65, -s * 0.62);
            ctx.quadraticCurveTo(-s * 0.1, -s * 0.92, s * 0.35, -s * 0.58);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();

            // Vibrant body gradient
            const clownGrad = ctx.createLinearGradient(0, -s * 0.68, 0, s * 0.68);
            clownGrad.addColorStop(0, '#ff8a50');
            clownGrad.addColorStop(0.5, bodyColor);
            clownGrad.addColorStop(1, '#c2410c');
            ctx.fillStyle = clownGrad;
            ctx.beginPath();
            ctx.ellipse(0, 0, s, s * 0.68, 0, 0, Math.PI * 2);
            ctx.fill();

            // Curved white bands with crisp borders
            ctx.fillStyle = stripeColor;
            ctx.strokeStyle = '#0f172a';
            ctx.lineWidth = 1.3;

            ctx.beginPath();
            ctx.ellipse(-2, 0, 4.4, s * 0.66, 0.05, 0, Math.PI * 2);
            ctx.fill();
            ctx.stroke();

            ctx.beginPath();
            ctx.ellipse(-s * 0.56, 0, 3.6, s * 0.52, -0.05, 0, Math.PI * 2);
            ctx.fill();
            ctx.stroke();

            this.drawGillArc(ctx, s * 0.36, s * 0.32, -Math.PI * 0.35, Math.PI * 0.35);

            // Pectoral fin
            ctx.fillStyle = stripeColor;
            ctx.beginPath();
            ctx.ellipse(s * 0.12, s * 0.2, 6, 3.4, finFlap * 0.08 - 0.15, 0, Math.PI * 2);
            ctx.fill();
            ctx.stroke();

            this.drawRealisticEye(ctx, s * 0.56, -s * 0.14, 4.2);

        } else if (this.type === 'tang') {
            const bodyColor = this.color1 || '#38bdf8';
            const finColor = this.color2 || '#fde047';

            // Bright yellow tail fin with rays
            ctx.fillStyle = finColor;
            ctx.beginPath();
            ctx.moveTo(-s * 0.92, 0);
            ctx.quadraticCurveTo(-s * 1.52, -s * 0.74 + wag, -s * 1.34, wag * 0.5);
            ctx.quadraticCurveTo(-s * 1.52, s * 0.74 + wag, -s * 0.92, 0);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s * 0.95, 0, -s * 1.35, -s * 0.45 + wag, 3, 0.25);
            this.drawFinRays(ctx, -s * 0.95, 0, -s * 1.35, s * 0.45 + wag, 3, 0.25);

            // Luminous blue body gradient
            const tangGrad = ctx.createLinearGradient(0, -s * 0.75, 0, s * 0.75);
            tangGrad.addColorStop(0, '#7dd3fc');
            tangGrad.addColorStop(0.5, bodyColor);
            tangGrad.addColorStop(1, '#1d4ed8');
            ctx.fillStyle = tangGrad;
            ctx.beginPath();
            ctx.ellipse(0, 0, s, s * 0.75, 0, 0, Math.PI * 2);
            ctx.fill();

            // Golden dorsal & anal fin trim
            ctx.fillStyle = finColor;
            ctx.beginPath();
            ctx.moveTo(-s * 0.62, -s * 0.66);
            ctx.quadraticCurveTo(-s * 0.05, -s * 1.05, s * 0.42, -s * 0.56);
            ctx.quadraticCurveTo(0, -s * 0.7, -s * 0.62, -s * 0.66);
            ctx.closePath();
            ctx.fill();

            // Signature palette crescent
            ctx.strokeStyle = '#0f172a';
            ctx.lineWidth = 2.6;
            ctx.beginPath();
            ctx.arc(-s * 0.04, 0, s * 0.46, 0.22, Math.PI - 0.22);
            ctx.stroke();

            this.drawGillArc(ctx, s * 0.42, s * 0.34, -Math.PI * 0.35, Math.PI * 0.35);
            this.drawRealisticEye(ctx, s * 0.58, -s * 0.14, 4.2);

        } else if (this.type === 'koi') {
            const baseColor = this.color1 || '#ffffff';
            const patchColor = this.color2 || '#fb7185';

            // Flowing translucent tail fin with delicate rays
            const koiTailGrad = ctx.createLinearGradient(-s * 1.08, 0, -s * 1.75, wag);
            koiTailGrad.addColorStop(0, '#ffffff');
            koiTailGrad.addColorStop(1, 'rgba(255, 255, 255, 0.65)');
            ctx.fillStyle = koiTailGrad;
            ctx.strokeStyle = patchColor;
            ctx.lineWidth = 1.4;
            ctx.beginPath();
            ctx.moveTo(-s * 1.08, 0);
            ctx.quadraticCurveTo(-s * 1.78, -s * 0.85 + wag, -s * 1.5, wag * 0.5);
            ctx.quadraticCurveTo(-s * 1.78, s * 0.85 + wag, -s * 1.08, 0);
            ctx.closePath();
            ctx.fill();
            ctx.stroke();
            this.drawFinRays(ctx, -s * 1.1, 0, -s * 1.55, -s * 0.5 + wag, 4, 0.22);
            this.drawFinRays(ctx, -s * 1.1, 0, -s * 1.55, s * 0.5 + wag, 4, 0.22);

            // Dorsal fin
            ctx.fillStyle = 'rgba(255, 255, 255, 0.88)';
            ctx.beginPath();
            ctx.moveTo(-s * 0.55, -s * 0.48);
            ctx.quadraticCurveTo(-s * 0.1, -s * 0.92, s * 0.25, -s * 0.46);
            ctx.closePath();
            ctx.fill();

            // Sleek koi body with top-to-belly shading
            const koiGrad = ctx.createLinearGradient(0, -s * 0.54, 0, s * 0.54);
            koiGrad.addColorStop(0, '#ffffff');
            koiGrad.addColorStop(0.65, baseColor);
            koiGrad.addColorStop(1, '#e2e8f0');
            ctx.fillStyle = koiGrad;
            ctx.beginPath();
            ctx.ellipse(0, 0, s * 1.15, s * 0.54, 0, 0, Math.PI * 2);
            ctx.fill();

            // Organic koi color patches
            ctx.fillStyle = patchColor;
            ctx.beginPath();
            ctx.ellipse(-s * 0.25, -s * 0.08, s * 0.46, s * 0.34, 0.15, 0, Math.PI * 2);
            ctx.fill();

            ctx.fillStyle = '#fb923c';
            ctx.beginPath();
            ctx.ellipse(s * 0.44, -s * 0.12, s * 0.28, s * 0.22, -0.1, 0, Math.PI * 2);
            ctx.fill();

            // Whisker barbels near mouth
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.85)';
            ctx.lineWidth = 1.2;
            ctx.beginPath();
            ctx.moveTo(s * 1.05, s * 0.08);
            ctx.quadraticCurveTo(s * 1.22, s * 0.18, s * 1.15, s * 0.28);
            ctx.stroke();

            this.drawGillArc(ctx, s * 0.52, s * 0.28, -Math.PI * 0.35, Math.PI * 0.35);

            // Pectoral fin
            ctx.fillStyle = 'rgba(255, 255, 255, 0.85)';
            ctx.beginPath();
            ctx.ellipse(s * 0.12, s * 0.22, 7.5, 3.6, finFlap * 0.08 - 0.15, 0, Math.PI * 2);
            ctx.fill();

            this.drawRealisticEye(ctx, s * 0.72, -s * 0.12, 4.0);

        } else if (this.type === 'goldfish') {
            const goldBody = this.color1 || '#f97316';
            const goldFin = this.color2 || '#fde047';

            // Flowing tail fin with rays
            const goldTailGrad = ctx.createLinearGradient(-s * 0.85, 0, -s * 1.55, wag);
            goldTailGrad.addColorStop(0, goldFin);
            goldTailGrad.addColorStop(1, 'rgba(254, 240, 138, 0.8)');
            ctx.fillStyle = goldTailGrad;
            ctx.beginPath();
            ctx.moveTo(-s * 0.85, 0);
            ctx.quadraticCurveTo(-s * 1.55, -s * 0.75 + wag, -s * 1.35, wag * 0.5);
            ctx.quadraticCurveTo(-s * 1.55, s * 0.75 + wag, -s * 0.85, 0);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s * 0.88, 0, -s * 1.38, -s * 0.45 + wag, 3, 0.24);
            this.drawFinRays(ctx, -s * 0.88, 0, -s * 1.38, s * 0.45 + wag, 3, 0.24);

            // Rounded golden body with warm gradient
            const goldGrad = ctx.createLinearGradient(0, -s * 0.72, 0, s * 0.72);
            goldGrad.addColorStop(0, '#fdba74');
            goldGrad.addColorStop(0.5, goldBody);
            goldGrad.addColorStop(1, '#ea580c');
            ctx.fillStyle = goldGrad;
            ctx.beginPath();
            ctx.ellipse(0, 0, s * 0.95, s * 0.72, 0, 0, Math.PI * 2);
            ctx.fill();

            // Dorsal fin
            ctx.fillStyle = goldFin;
            ctx.beginPath();
            ctx.moveTo(-s * 0.25, -s * 0.65);
            ctx.quadraticCurveTo(0, -s * 1.15, s * 0.3, -s * 0.6);
            ctx.closePath();
            ctx.fill();
            this.drawFinRays(ctx, -s * 0.15, -s * 0.62, s * 0.05, -s * 1.0, 3, 0.22);

            // Pectoral fin
            ctx.fillStyle = goldFin;
            ctx.beginPath();
            ctx.ellipse(-s * 0.05, s * 0.2, 6, 3.5, finFlap * 0.08, 0, Math.PI * 2);
            ctx.fill();

            this.drawGillArc(ctx, s * 0.32, s * 0.34, -Math.PI * 0.35, Math.PI * 0.35);
            this.drawRealisticEye(ctx, s * 0.44, -s * 0.15, 4.4);
        }

        ctx.restore();
    }
}