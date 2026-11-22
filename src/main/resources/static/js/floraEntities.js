export const allFloraCatalog = [
    {
        id: 1,
        type: 'kelp',
        name: 'Giant Emerald Kelp',
        height: 82,
        color: '#22c55e',
        accentColor: '#86efac',
        curve: 24
    },
    {
        id: 2,
        type: 'bush',
        name: 'Sea Lettuce Bush',
        height: 36,
        color: '#4ade80',
        accentColor: '#bbf7d0',
        curve: 0
    },
    {
        id: 3,
        type: 'seagrass',
        name: 'Vibrant Ribbon Seagrass',
        height: 58,
        color: '#10b981',
        accentColor: '#6ee7b7',
        curve: -20
    },
    {
        id: 4,
        type: 'kelp',
        name: 'Deepwater Coral Kelp',
        height: 74,
        color: '#16a34a',
        accentColor: '#4ade80',
        curve: 28
    },
    {
        id: 5,
        type: 'fern',
        name: 'Java Lace Fern',
        height: 52,
        color: '#34d399',
        accentColor: '#a7f3d0',
        curve: 12
    },
    {
        id: 6,
        type: 'anubias',
        name: 'Golden Anubias',
        height: 42,
        color: '#84cc16',
        accentColor: '#bef264',
        curve: -10
    },
    {
        id: 7,
        type: 'foxTail',
        name: 'Crimson Foxtail',
        height: 66,
        color: '#f43f5e',
        accentColor: '#fda4af',
        curve: 16
    }
];

const SLOT_RATIOS = [0.14, 0.36, 0.62, 0.84];
const SESSION_FLORA_KEY = 'pennypals_session_flora_ids';

function pickRandomFourFlora() {
    const pool = [...allFloraCatalog];
    for (let i = pool.length - 1; i > 0; i--) {
        const j = Math.floor(Math.random() * (i + 1));
        [pool[i], pool[j]] = [pool[j], pool[i]];
    }

    return pool.slice(0, 4).map((flora, index) => ({
        ...flora,
        slotIndex: index,
        offsetRatio: SLOT_RATIOS[index]
    }));
}

function buildFloraFromIds(ids) {
    const byId = new Map(allFloraCatalog.map(item => [item.id, item]));
    const picked = ids
        .map(id => byId.get(Number(id)))
        .filter(Boolean)
        .slice(0, 4);

    if (picked.length < 4) {
        return null;
    }

    return picked.map((flora, index) => ({
        ...flora,
        slotIndex: index,
        offsetRatio: SLOT_RATIOS[index]
    }));
}

export let equippedFlora = pickRandomFourFlora();

// Call on aquarium load: reuses the session's 4 flora if already chosen, or rolls once per session
export function rollLoginFlora() {
    try {
        const savedRaw = sessionStorage.getItem(SESSION_FLORA_KEY);
        if (savedRaw) {
            const parsedIds = JSON.parse(savedRaw);
            if (Array.isArray(parsedIds)) {
                const restored = buildFloraFromIds(parsedIds);
                if (restored) {
                    equippedFlora = restored;
                    return equippedFlora;
                }
            }
        }
    } catch (_) {
        // Fall through to a fresh roll if sessionStorage is unavailable or invalid
    }

    const fresh = pickRandomFourFlora();
    try {
        sessionStorage.setItem(SESSION_FLORA_KEY, JSON.stringify(fresh.map(f => f.id)));
    } catch (_) {
        // Ignore storage quota or privacy restrictions
    }
    equippedFlora = fresh;
    return equippedFlora;
}

// Clear stored session flora so the next login rolls a new random set of 4
export function clearSessionFlora() {
    try {
        sessionStorage.removeItem(SESSION_FLORA_KEY);
    } catch (_) {
        // Ignore storage quota or privacy restrictions
    }
}

function quadPoint(t, p0, p1, p2) {
    const inv = 1 - t;
    return inv * inv * p0 + 2 * inv * t * p1 + t * t * p2;
}

function drawRootAnchor(ctx, spread = 12, color = '#3f2e21') {
    ctx.save();
    ctx.strokeStyle = color;
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.beginPath();
    ctx.moveTo(0, -1);
    ctx.quadraticCurveTo(-spread * 0.5, 1, -spread, 2);
    ctx.moveTo(0, -1);
    ctx.quadraticCurveTo(spread * 0.5, 1, spread, 2);
    ctx.stroke();

    ctx.fillStyle = 'rgba(15, 23, 42, 0.28)';
    ctx.beginPath();
    ctx.ellipse(0, 1, spread * 1.15, 3, 0, 0, Math.PI * 2);
    ctx.fill();
    ctx.restore();
}

function drawLeafBlade(ctx, bx, by, length, width, angle, fillStyle, veinColor = 'rgba(255,255,255,0.35)') {
    ctx.save();
    ctx.translate(bx, by);
    ctx.rotate(angle);

    ctx.fillStyle = fillStyle;
    ctx.beginPath();
    ctx.moveTo(0, 0);
    ctx.quadraticCurveTo(length * 0.5, -width, length, 0);
    ctx.quadraticCurveTo(length * 0.5, width, 0, 0);
    ctx.closePath();
    ctx.fill();

    // Central midrib vein
    ctx.strokeStyle = veinColor;
    ctx.lineWidth = 0.9;
    ctx.beginPath();
    ctx.moveTo(1, 0);
    ctx.lineTo(length * 0.85, 0);
    ctx.stroke();

    ctx.restore();
}

export function renderFloraEntity(ctx, flora, floorY, time = 0) {
    const fx = ctx.canvas.width * (flora.offsetRatio ?? 0.5);
    const sway = Math.sin(time * 0.0022 + (flora.slotIndex || 0) * 1.4) * 6;
    const h = flora.height || 50;
    const c = flora.curve || 0;
    const baseColor = flora.color || '#22c55e';
    const accentColor = flora.accentColor || '#86efac';

    ctx.save();
    // Anchor y = 0 directly on top of the sand floor
    ctx.translate(fx, floorY);
    drawRootAnchor(ctx, 11);

    if (flora.type === 'kelp') {
        const ctrlX = c + sway * 0.55;
        const tipX = c * 0.68 + sway;

        const stipeGrad = ctx.createLinearGradient(0, 0, tipX, -h);
        stipeGrad.addColorStop(0, '#14532d');
        stipeGrad.addColorStop(0.55, baseColor);
        stipeGrad.addColorStop(1, accentColor);

        // Main stipe (stalk)
        ctx.strokeStyle = stipeGrad;
        ctx.lineWidth = 4.6;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(ctrlX, -h * 0.5, tipX, -h);
        ctx.stroke();

        // Gas bladders (pneumatocysts) & veined kelp blades
        const nodes = [0.24, 0.46, 0.68, 0.88];
        nodes.forEach((t, idx) => {
            const bx = quadPoint(t, 0, ctrlX, tipX);
            const by = quadPoint(t, 0, -h * 0.5, -h);
            const side = idx % 2 === 0 ? 1 : -1;
            const bladeLen = (15 - idx * 2) * (1 - t * 0.2);

            // Air bladder bulb
            ctx.fillStyle = accentColor;
            ctx.beginPath();
            ctx.arc(bx, by, 2.3, 0, Math.PI * 2);
            ctx.fill();

            drawLeafBlade(
                ctx,
                bx,
                by,
                bladeLen,
                4.2,
                side * (0.45 + Math.sin(time * 0.003 + idx) * 0.08) - (side < 0 ? Math.PI : 0),
                idx % 2 === 0 ? baseColor : accentColor
            );
        });

    } else if (flora.type === 'seagrass') {
        const bladeOffsets = [-9, -3, 3, 9];
        bladeOffsets.forEach((ox, idx) => {
            const bh = h * (0.76 + (idx % 2) * 0.24);
            const localSway = sway * (0.75 + idx * 0.1);
            const ctrlX = ox + c * 0.6 + localSway * 0.55;
            const tipX = ox + c * 0.45 + localSway;

            const bladeGrad = ctx.createLinearGradient(ox, 0, tipX, -bh);
            bladeGrad.addColorStop(0, '#065f46');
            bladeGrad.addColorStop(0.6, idx % 2 === 0 ? baseColor : accentColor);
            bladeGrad.addColorStop(1, accentColor);

            // Tapered ribbon blade
            ctx.fillStyle = bladeGrad;
            ctx.beginPath();
            ctx.moveTo(ox - 2.3, 0);
            ctx.quadraticCurveTo(ctrlX - 1.6, -bh * 0.55, tipX, -bh);
            ctx.quadraticCurveTo(ctrlX + 1.8, -bh * 0.55, ox + 2.3, 0);
            ctx.closePath();
            ctx.fill();

            // Subtle longitudinal rib
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.25)';
            ctx.lineWidth = 0.8;
            ctx.beginPath();
            ctx.moveTo(ox, -2);
            ctx.quadraticCurveTo(ctrlX, -bh * 0.5, tipX, -bh * 0.85);
            ctx.stroke();
        });

    } else if (flora.type === 'bush') {
        const bushGrad = ctx.createRadialGradient(0, -h * 0.45, 4, 0, -h * 0.4, h * 0.75);
        bushGrad.addColorStop(0, accentColor);
        bushGrad.addColorStop(0.65, baseColor);
        bushGrad.addColorStop(1, '#15803d');

        // Ruffled multi-lobe sea-lettuce foliage
        const lobes = [
            { x: -11, y: -9,  rx: 9,  ry: 7.5 },
            { x: 11,  y: -9,  rx: 9,  ry: 7.5 },
            { x: -7,  y: -17, rx: 9.5, ry: 8 },
            { x: 7,   y: -17, rx: 9.5, ry: 8 },
            { x: 0,   y: -22, rx: 11, ry: 8.5 },
            { x: 0,   y: -11, rx: 12, ry: 8 }
        ];

        lobes.forEach((lb, idx) => {
            const lx = lb.x + sway * (0.2 + idx * 0.04);
            ctx.fillStyle = idx % 2 === 0 ? bushGrad : baseColor;
            ctx.beginPath();
            ctx.ellipse(lx, lb.y, lb.rx, lb.ry, (idx - 2.5) * 0.12, 0, Math.PI * 2);
            ctx.fill();

            // Leaf wrinkle highlight
            ctx.strokeStyle = 'rgba(255, 255, 255, 0.28)';
            ctx.lineWidth = 1;
            ctx.beginPath();
            ctx.arc(lx, lb.y, lb.rx * 0.55, Math.PI * 1.15, Math.PI * 1.75);
            ctx.stroke();
        });

    } else if (flora.type === 'fern') {
        const angles = [-0.78, -0.39, 0, 0.39, 0.78];
        angles.forEach((ang, idx) => {
            const frondLen = h * (idx === 2 ? 1.0 : 0.85);
            const tipX = Math.sin(ang) * frondLen + sway * 0.55;
            const tipY = -Math.cos(ang) * frondLen;
            const ctrlX = tipX * 0.52;
            const ctrlY = tipY * 0.65;

            // Central rachis stem
            ctx.strokeStyle = baseColor;
            ctx.lineWidth = 2.2;
            ctx.lineCap = 'round';
            ctx.beginPath();
            ctx.moveTo(0, 0);
            ctx.quadraticCurveTo(ctrlX, ctrlY, tipX, tipY);
            ctx.stroke();

            // Paired pinnae leaflets along each frond
            [0.32, 0.55, 0.78].forEach((t, pIdx) => {
                const px = quadPoint(t, 0, ctrlX, tipX);
                const py = quadPoint(t, 0, ctrlY, tipY);
                const leafletLen = (8 - pIdx * 1.4);

                drawLeafBlade(ctx, px, py, leafletLen, 2.5, ang - Math.PI * 0.8, baseColor);
                drawLeafBlade(ctx, px, py, leafletLen, 2.5, ang - Math.PI * 0.2, accentColor);
            });

            // Tip leaflet
            drawLeafBlade(ctx, tipX, tipY, 7, 2.4, ang - Math.PI * 0.5, accentColor);
        });

    } else if (flora.type === 'anubias') {
        const leaves = [
            { x: -12, y: -15, len: 15, wid: 5.5, rot: -2.45 },
            { x: 12,  y: -15, len: 15, wid: 5.5, rot: -0.68 },
            { x: -7,  y: -27, len: 17, wid: 6.2, rot: -2.05 },
            { x: 7,   y: -26, len: 16, wid: 5.8, rot: -1.05 },
            { x: 0,   y: -33, len: 16, wid: 6.0, rot: -1.57 }
        ];

        leaves.forEach((lf, idx) => {
            const lx = lf.x + sway * 0.35;
            const ly = lf.y;

            // Petiole stem
            ctx.strokeStyle = '#4d7c0f';
            ctx.lineWidth = 2.1;
            ctx.beginPath();
            ctx.moveTo(0, -1);
            ctx.quadraticCurveTo(lx * 0.5, ly * 0.55, lx, ly);
            ctx.stroke();

            const leafGrad = ctx.createLinearGradient(0, 0, lf.len, 0);
            leafGrad.addColorStop(0, baseColor);
            leafGrad.addColorStop(1, accentColor);

            drawLeafBlade(ctx, lx, ly, lf.len, lf.wid, lf.rot + sway * 0.015, leafGrad);
        });

    } else if (flora.type === 'foxTail') {
        const ctrlX = c * 0.5 + sway * 0.55;
        const topX = c * 0.4 + sway;

        const stemGrad = ctx.createLinearGradient(0, 0, topX, -h);
        stemGrad.addColorStop(0, '#9f1239');
        stemGrad.addColorStop(0.5, baseColor);
        stemGrad.addColorStop(1, accentColor);

        ctx.strokeStyle = stemGrad;
        ctx.lineWidth = 2.8;
        ctx.lineCap = 'round';
        ctx.beginPath();
        ctx.moveTo(0, 0);
        ctx.quadraticCurveTo(ctrlX, -h * 0.5, topX, -h);
        ctx.stroke();

        // Dense feathery whorls along stem
        const whorls = 8;
        for (let step = 1; step <= whorls; step++) {
            const frac = step / (whorls + 0.6);
            const wy = quadPoint(frac, 0, -h * 0.5, -h);
            const wx = quadPoint(frac, 0, ctrlX, topX);
            const scale = 1 - frac * 0.32;
            const fill = step % 2 === 0 ? baseColor : accentColor;

            // Left & right feathery sub-bristles
            [-1, 1].forEach(dir => {
                for (let b = -1; b <= 1; b++) {
                    const bristleAngle = dir < 0
                        ? Math.PI + b * 0.28 + sway * 0.02
                        : b * 0.28 + sway * 0.02;
                    drawLeafBlade(ctx, wx, wy, 10.5 * scale, 2.2 * scale, bristleAngle, fill, 'rgba(255,255,255,0.25)');
                }
            });
        }
    }

    ctx.restore();
}