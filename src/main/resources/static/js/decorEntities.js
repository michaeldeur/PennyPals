export const availableDecors = {
    CHEST: { id: 'CHEST', name: 'Sunken Treasure Chest', description: 'Old oak chest overflowing with golden coins.' },
    COLUMN: { id: 'COLUMN', name: 'Ancient Stone Column', description: 'Weathered Greco-Roman pillar with moss and cracks.' },
    STATUE: { id: 'STATUE', name: 'Mystic Tiki Monolith', description: 'Carved volcanic stone face guarding the deep.' },
    GEODE: { id: 'GEODE', name: 'Shimmering Crystal Geode', description: 'Bioluminescent mineral cluster glowing softly.' },
    ANCHOR: { id: 'ANCHOR', name: 'Rusted Iron Anchor', description: 'Heavy nautical anchor wrapped in ancient chains.' },
    ARCHWAY: { id: 'ARCHWAY', name: 'Coral Reef Archway', description: 'Natural stone bridge covered in colorful polyps.' },
    WHEEL: { id: 'WHEEL', name: "Captain's Shipwreck Helm", description: 'A classic wooden steering wheel from a lost vessel.' }
};

export let userOrderedSlots = [
    { slotIndex: 0, ratio: 0.25, selectedItem: 'CHEST' },
    { slotIndex: 1, ratio: 0.50, selectedItem: 'COLUMN' },
    { slotIndex: 2, ratio: 0.75, selectedItem: 'GEODE' }
];

export function setUserDecorOrder(item1, item2, item3) {
    userOrderedSlots[0].selectedItem = item1;
    userOrderedSlots[1].selectedItem = item2;
    userOrderedSlots[2].selectedItem = item3;
}

function resolveDecorKey(rawItem) {
    const item = (rawItem || 'CHEST').toUpperCase();
    if (item.includes('CHEST') || item.includes('TREASURE')) return 'CHEST';
    if (item.includes('COLUMN') || item.includes('PILLAR')) return 'COLUMN';
    if (item.includes('STATUE') || item.includes('MONOLITH') || item.includes('TIKI')) return 'STATUE';
    if (item.includes('GEODE') || item.includes('CRYSTAL')) return 'GEODE';
    if (item.includes('ANCHOR')) return 'ANCHOR';
    if (item.includes('ARCHWAY') || item.includes('BRIDGE')) return 'ARCHWAY';
    if (item.includes('WHEEL') || item.includes('HELM')) return 'WHEEL';
    return 'CHEST';
}

export function renderDecorSlot(ctx, slot, floorY) {
    const dx = ctx.canvas.width * (slot.ratio ?? 0.5);
    // Anchor local y = 0 directly on the top surface of the sand/floor
    ctx.save();
    ctx.translate(dx, floorY);

    const item = resolveDecorKey(slot.selectedItem || slot.itemName || 'CHEST');

    // Soft contact shadow along top of sand
    ctx.fillStyle = 'rgba(15, 23, 42, 0.28)';
    ctx.beginPath();
    ctx.ellipse(0, 0, 26, 4, 0, 0, Math.PI * 2);
    ctx.fill();

    if (item === 'CHEST') {
        // Base box (bottom at y = 0)
        const baseGrad = ctx.createLinearGradient(0, -20, 0, 0);
        baseGrad.addColorStop(0, slot.colorHex || '#7c4724');
        baseGrad.addColorStop(1, '#3b1e0c');
        ctx.fillStyle = baseGrad;
        ctx.beginPath();
        ctx.roundRect(-20, -20, 40, 20, 3);
        ctx.fill();

        // Plank seams & gold corner brackets
        ctx.strokeStyle = 'rgba(24, 12, 4, 0.55)';
        ctx.lineWidth = 1.2;
        ctx.beginPath();
        ctx.moveTo(-18, -13); ctx.lineTo(18, -13);
        ctx.moveTo(-18, -7); ctx.lineTo(18, -7);
        ctx.stroke();

        // Overflowing gold coins peeking out
        ctx.fillStyle = '#fde047';
        [-10, -4, 3, 9].forEach((cx, i) => {
            ctx.beginPath();
            ctx.arc(cx, -21 - (i % 2) * 2, 4.2, 0, Math.PI * 2);
            ctx.fill();
        });

        // Domed lid
        const lidGrad = ctx.createLinearGradient(0, -28, 0, -19);
        lidGrad.addColorStop(0, '#9a5b2f');
        lidGrad.addColorStop(1, '#5c3317');
        ctx.fillStyle = lidGrad;
        ctx.beginPath();
        ctx.roundRect(-22, -27, 44, 8, [6, 6, 2, 2]);
        ctx.fill();

        // Gold bands & lock plate
        ctx.fillStyle = '#fbbf24';
        ctx.fillRect(-16, -27, 3.5, 27);
        ctx.fillRect(12.5, -27, 3.5, 27);
        ctx.fillRect(-5.5, -18, 11, 9);

        ctx.fillStyle = '#1e293b';
        ctx.beginPath();
        ctx.arc(0, -14, 1.8, 0, Math.PI * 2);
        ctx.fill();

    } else if (item === 'COLUMN') {
        const stoneBase = slot.colorHex || '#94a3b8';

        // Plinth base resting at y = 0
        ctx.fillStyle = '#cbd5e1';
        ctx.fillRect(-16, -7, 32, 7);
        ctx.fillStyle = '#64748b';
        ctx.fillRect(-14, -10, 28, 3);

        // Fluted shaft (-52 to -10)
        const shaftGrad = ctx.createLinearGradient(-12, 0, 12, 0);
        shaftGrad.addColorStop(0, '#64748b');
        shaftGrad.addColorStop(0.45, stoneBase);
        shaftGrad.addColorStop(1, '#475569');
        ctx.fillStyle = shaftGrad;
        ctx.fillRect(-12, -52, 24, 42);

        // Fluting grooves
        ctx.strokeStyle = 'rgba(15, 23, 42, 0.35)';
        ctx.lineWidth = 1.2;
        [-6, 0, 6].forEach(gx => {
            ctx.beginPath();
            ctx.moveTo(gx, -50);
            ctx.lineTo(gx, -12);
            ctx.stroke();
        });

        // Subtle crack & moss patch
        ctx.strokeStyle = 'rgba(30, 41, 59, 0.5)';
        ctx.beginPath();
        ctx.moveTo(4, -36); ctx.lineTo(1, -29); ctx.lineTo(5, -23);
        ctx.stroke();

        ctx.fillStyle = '#4ade80';
        ctx.beginPath();
        ctx.ellipse(-9, -12, 5, 3, -0.2, 0, Math.PI * 2);
        ctx.fill();

        // Capital crown (-60 to -52)
        ctx.fillStyle = '#e2e8f0';
        ctx.beginPath();
        ctx.roundRect(-16, -60, 32, 8, 2);
        ctx.fill();

    } else if (item === 'STATUE') {
        const monolithColor = slot.colorHex || '#475569';

        // Carved volcanic monolith (bottom at y = 0)
        const statueGrad = ctx.createLinearGradient(-16, -54, 16, 0);
        statueGrad.addColorStop(0, '#64748b');
        statueGrad.addColorStop(0.6, monolithColor);
        statueGrad.addColorStop(1, '#1e293b');
        ctx.fillStyle = statueGrad;
        ctx.beginPath();
        ctx.moveTo(-17, 0);
        ctx.lineTo(-13, -52);
        ctx.quadraticCurveTo(0, -58, 13, -52);
        ctx.lineTo(17, 0);
        ctx.closePath();
        ctx.fill();

        // Brow ridge & carved nose
        ctx.fillStyle = '#1e293b';
        ctx.fillRect(-11, -43, 22, 4);

        ctx.fillStyle = '#94a3b8';
        ctx.beginPath();
        ctx.moveTo(0, -39);
        ctx.lineTo(-3.5, -27);
        ctx.lineTo(3.5, -27);
        ctx.closePath();
        ctx.fill();

        // Glowing cyan eyes
        ctx.fillStyle = '#38bdf8';
        ctx.beginPath();
        ctx.roundRect(-9, -38, 6, 4.5, 1.5);
        ctx.roundRect(3, -38, 6, 4.5, 1.5);
        ctx.fill();

        // Carved mouth & geometric markings
        ctx.strokeStyle = '#0f172a';
        ctx.lineWidth = 2;
        ctx.beginPath();
        ctx.moveTo(-8, -19); ctx.lineTo(8, -19);
        ctx.moveTo(-5, -19); ctx.lineTo(-5, -15);
        ctx.moveTo(0, -19); ctx.lineTo(0, -15);
        ctx.moveTo(5, -19); ctx.lineTo(5, -15);
        ctx.stroke();

    } else if (item === 'GEODE') {
        const rockColor = slot.colorHex || '#57534e';

        // Outer mineral rock shell resting at y = 0
        const rockGrad = ctx.createLinearGradient(0, -28, 0, 0);
        rockGrad.addColorStop(0, '#78716c');
        rockGrad.addColorStop(1, rockColor);
        ctx.fillStyle = rockGrad;
        ctx.beginPath();
        ctx.ellipse(0, -12, 22, 12, 0, 0, Math.PI * 2);
        ctx.fill();

        // Inner cavity & glowing amethyst crystal spires
        ctx.fillStyle = '#3b0764';
        ctx.beginPath();
        ctx.ellipse(0, -13, 15, 8.5, 0, 0, Math.PI * 2);
        ctx.fill();

        const crystals = [
            { x: -8, y: -13, tipX: -12, tipY: -25, c: '#e879f9' },
            { x: -2, y: -13, tipX: -2, tipY: -31, c: '#c084fc' },
            { x: 5, y: -13, tipX: 8, tipY: -27, c: '#a855f7' },
            { x: 10, y: -13, tipX: 14, tipY: -21, c: '#f0abfc' }
        ];
        crystals.forEach(cr => {
            ctx.fillStyle = cr.c;
            ctx.beginPath();
            ctx.moveTo(cr.x - 3.5, cr.y);
            ctx.lineTo(cr.tipX, cr.tipY);
            ctx.lineTo(cr.x + 3.5, cr.y);
            ctx.closePath();
            ctx.fill();
        });

    } else if (item === 'ANCHOR') {
        const ironColor = slot.colorHex || '#334155';
        ctx.strokeStyle = ironColor;
        ctx.lineCap = 'round';
        ctx.lineJoin = 'round';

        // Top ring & shackle
        ctx.lineWidth = 3.2;
        ctx.beginPath();
        ctx.arc(0, -49, 5.5, 0, Math.PI * 2);
        ctx.stroke();

        // Crossbar stock
        ctx.lineWidth = 3.5;
        ctx.beginPath();
        ctx.moveTo(-14, -38);
        ctx.lineTo(14, -38);
        ctx.stroke();

        // Main shank down to curved fluke (bottom of fluke at y = 0)
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.moveTo(0, -44);
        ctx.lineTo(0, -4);
        ctx.stroke();

        ctx.beginPath();
        ctx.moveTo(-21, -13);
        ctx.quadraticCurveTo(0, 3, 21, -13);
        ctx.stroke();

        // Arrowhead flukes
        ctx.fillStyle = ironColor;
        ctx.beginPath();
        ctx.moveTo(-21, -13); ctx.lineTo(-24, -7); ctx.lineTo(-16, -9);
        ctx.closePath();
        ctx.fill();

        ctx.beginPath();
        ctx.moveTo(21, -13); ctx.lineTo(24, -7); ctx.lineTo(16, -9);
        ctx.closePath();
        ctx.fill();

    } else if (item === 'ARCHWAY') {
        const archColor = slot.colorHex || '#db2777';

        // Stone/coral arch span with both legs meeting y = 0
        ctx.strokeStyle = archColor;
        ctx.lineWidth = 11;
        ctx.lineCap = 'butt';
        ctx.beginPath();
        ctx.arc(0, -5.5, 24, Math.PI, 0, false);
        ctx.stroke();

        // Subtle highlight trim along arch
        ctx.strokeStyle = 'rgba(255, 255, 255, 0.35)';
        ctx.lineWidth = 2.5;
        ctx.beginPath();
        ctx.arc(0, -5.5, 27, Math.PI * 1.08, -Math.PI * 0.08, false);
        ctx.stroke();

        // Colorful reef polyps along the archway
        const polyps = [
            { angle: Math.PI * 1.15, color: '#fbbf24', r: 3.2 },
            { angle: Math.PI * 1.35, color: '#38bdf8', r: 3.5 },
            { angle: Math.PI * 1.5,  color: '#f472b6', r: 4.0 },
            { angle: Math.PI * 1.68, color: '#a3e635', r: 3.2 },
            { angle: Math.PI * 1.85, color: '#fb923c', r: 3.2 }
        ];
        polyps.forEach(p => {
            const px = Math.cos(p.angle) * 29;
            const py = -5.5 + Math.sin(p.angle) * 29;
            ctx.fillStyle = p.color;
            ctx.beginPath();
            ctx.arc(px, py, p.r, 0, Math.PI * 2);
            ctx.fill();
        });

    } else if (item === 'WHEEL') {
        const woodColor = slot.colorHex || '#78350f';
        const cy = -24;
        const r = 16;

        // Pedestal post resting at y = 0
        ctx.fillStyle = '#451a03';
        ctx.fillRect(-3.5, cy + r - 2, 7, 10);
        ctx.fillStyle = woodColor;
        ctx.fillRect(-7, -4, 14, 4);

        // Outer handles
        ctx.strokeStyle = '#92400e';
        ctx.lineWidth = 3;
        for (let i = 0; i < 6; i++) {
            const ang = (i * Math.PI) / 3;
            ctx.beginPath();
            ctx.moveTo(Math.cos(ang) * (r - 1), cy + Math.sin(ang) * (r - 1));
            ctx.lineTo(Math.cos(ang) * (r + 5), cy + Math.sin(ang) * (r + 5));
            ctx.stroke();
        }

        // Spokes
        ctx.strokeStyle = woodColor;
        ctx.lineWidth = 2.6;
        for (let i = 0; i < 6; i++) {
            const ang = (i * Math.PI) / 3;
            ctx.beginPath();
            ctx.moveTo(0, cy);
            ctx.lineTo(Math.cos(ang) * r, cy + Math.sin(ang) * r);
            ctx.stroke();
        }

        // Outer wheel rim
        ctx.strokeStyle = woodColor;
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.arc(0, cy, r, 0, Math.PI * 2);
        ctx.stroke();

        // Center hub
        ctx.fillStyle = '#fbbf24';
        ctx.beginPath();
        ctx.arc(0, cy, 5, 0, Math.PI * 2);
        ctx.fill();

    } else {
        ctx.fillStyle = slot.colorHex || '#64748b';
        ctx.beginPath();
        ctx.ellipse(0, -10, 18, 10, 0, 0, Math.PI * 2);
        ctx.fill();
    }

    ctx.restore();
}