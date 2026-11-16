// floraEntities.js - Detailed flora and aquatic plant configurations
export const equippedFlora = [
    {
        id: 1,
        type: 'kelp',
        name: 'Giant Emerald Kelp',
        offsetRatio: 0.15,
        height: 75,
        color: '#1e6b36',
        curve: 25
    },
    {
        id: 2,
        type: 'bush',
        name: 'Sea Lettuce Bush',
        offsetRatio: 0.38,
        height: 40,
        color: '#3cb371',
        curve: 0
    },
    {
        id: 3,
        type: 'seagrass',
        name: 'Vibrant Ribbon Seagrass',
        offsetRatio: 0.62,
        height: 55,
        color: '#2e8b57',
        curve: -20
    },
    {
        id: 4,
        type: 'kelp',
        name: 'Deepwater Coral Kelp',
        offsetRatio: 0.88,
        height: 70,
        color: '#228b22',
        curve: 30
    }
];

export function renderFloraEntity(ctx, flora, floorY) {
    let fx = ctx.canvas.width * flora.offsetRatio;

    if (flora.type === 'kelp' || flora.type === 'seagrass') {
        ctx.strokeStyle = flora.color;
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.moveTo(fx, floorY);
        ctx.quadraticCurveTo(fx + flora.curve, floorY - (flora.height * 0.5), fx + (flora.curve * 0.5), floorY - flora.height);
        ctx.stroke();
    } else if (flora.type === 'bush') {
        ctx.fillStyle = flora.color;
        for (let b = 0; b < 3; b++) {
            ctx.beginPath();
            ctx.arc(fx + (b * 8) - 8, floorY - 10 - (b * 5), 12, 0, Math.PI * 2);
            ctx.fill();
        }
    }
}