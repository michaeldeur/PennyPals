// decorEntities.js - 7 Intricate Decor Choices & 3-Slot Ordered Configuration
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

export function renderDecorSlot(ctx, slot, floorY) {
    let dx = ctx.canvas.width * slot.ratio;
    ctx.save();
    ctx.translate(dx, floorY - 5);

    // Normalize item name or key to handle database values seamlessly
    let rawItem = slot.selectedItem || slot.itemName || 'CHEST';
    let item = rawItem.toUpperCase();
    if (item.includes('CHEST') || item.includes('TREASURE')) item = 'CHEST';
    else if (item.includes('COLUMN') || item.includes('PILLAR')) item = 'COLUMN';
    else if (item.includes('STATUE') || item.includes('MONOLITH') || item.includes('TIKI')) item = 'STATUE';
    else if (item.includes('GEODE') || item.includes('CRYSTAL')) item = 'GEODE';
    else if (item.includes('ANCHOR')) item = 'ANCHOR';
    else if (item.includes('ARCHWAY') || item.includes('BRIDGE')) item = 'ARCHWAY';
    else if (item.includes('WHEEL') || item.includes('HELM')) item = 'WHEEL';

    if (item === 'CHEST') {
        ctx.fillStyle = "#3d2314";
        ctx.fillRect(-20, -20, 40, 22);
        ctx.fillStyle = "#5c3a21";
        ctx.fillRect(-22, -24, 44, 6);
        ctx.fillStyle = "#ffd700";
        ctx.fillRect(-6, -14, 12, 10);
        ctx.fillStyle = "#111111";
        ctx.fillRect(-2, -11, 4, 4);
    }
    else if (item === 'COLUMN') {
        ctx.fillStyle = slot.colorHex || "#94a3b8";
        ctx.fillRect(-12, -55, 24, 55);
        ctx.strokeStyle = "#475569";
        ctx.lineWidth = 1.5;
        ctx.beginPath();
        ctx.moveTo(-5, -55); ctx.lineTo(-5, 0);
        ctx.moveTo(5, -55); ctx.lineTo(5, 0);
        ctx.stroke();
        ctx.fillStyle = "#cbd5e1";
        ctx.fillRect(-16, -62, 32, 8);
    }
    else if (item === 'STATUE') {
        ctx.fillStyle = slot.colorHex || "#475569";
        ctx.beginPath();
        ctx.moveTo(-16, 0);
        ctx.lineTo(-12, -50);
        ctx.lineTo(12, -50);
        ctx.lineTo(16, 0);
        ctx.closePath();
        ctx.fill();
        ctx.fillStyle = "#38bdf8";
        ctx.fillRect(-8, -38, 5, 5);
        ctx.fillRect(3, -38, 5, 5);
    }
    else if (item === 'GEODE') {
        ctx.fillStyle = slot.colorHex || "#57534e";
        ctx.beginPath();
        ctx.ellipse(0, -15, 22, 18, 0, 0, Math.PI * 2);
        ctx.fill();
        ctx.fillStyle = "#c084fc";
        ctx.beginPath();
        ctx.moveTo(0, -32); ctx.lineTo(-10, -12); ctx.lineTo(0, -18); ctx.lineTo(10, -12);
        ctx.closePath();
        ctx.fill();
    }
    else if (item === 'ANCHOR') {
        ctx.strokeStyle = slot.colorHex || "#334155";
        ctx.lineWidth = 5;
        ctx.beginPath();
        ctx.moveTo(0, -45); ctx.lineTo(0, -5);
        ctx.stroke();
        ctx.beginPath();
        ctx.arc(0, -48, 6, 0, Math.PI * 2);
        ctx.stroke();
        ctx.beginPath();
        ctx.moveTo(-15, -35); ctx.lineTo(15, -35);
        ctx.moveTo(-22, -2); ctx.quadraticCurveTo(0, 10, 22, -2);
        ctx.stroke();
    }
    else if (item === 'ARCHWAY') {
        ctx.strokeStyle = slot.colorHex || "#db2777";
        ctx.lineWidth = 12;
        ctx.beginPath();
        ctx.arc(0, 0, 30, Math.PI, 0, false);
        ctx.stroke();
    }
    else if (item === 'WHEEL') {
        ctx.strokeStyle = slot.colorHex || "#78350f";
        ctx.lineWidth = 4;
        ctx.beginPath();
        ctx.arc(0, -25, 18, 0, Math.PI * 2);
        ctx.stroke();
        ctx.fillStyle = "#b45309";
        ctx.beginPath();
        ctx.arc(0, -25, 5, 0, Math.PI * 2);
        ctx.fill();
    }
    else {
        ctx.fillStyle = slot.colorHex || "#64748b";
        ctx.beginPath();
        ctx.ellipse(0, -8, 18, 12, 0, 0, Math.PI * 2);
        ctx.fill();
    }

    ctx.restore();
}