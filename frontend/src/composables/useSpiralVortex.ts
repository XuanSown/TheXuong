import { type Ref, onMounted, onUnmounted } from 'vue'

const VERT = `
attribute vec2 aPos;
varying vec2 vUv;
void main() {
  vUv = aPos * 0.5 + 0.5;
  gl_Position = vec4(aPos, 0.0, 1.0);
}
`

// ponytail: Spiral Vortex (silver pearl) — domain-warp FBM, centerless flow, cool-silver palette.
// Ceiling: 6-octave FBM at 1080p ~fine on integrated GPUs; drop octaves to 4 if mobile janks.
const FRAG = `
precision highp float;
uniform vec2 uResolution;
uniform float uTime;
varying vec2 vUv;

float hash(vec2 p) { return fract(sin(dot(p, vec2(127.1, 311.7))) * 43758.5453); }

float noise(vec2 p) {
  vec2 i = floor(p);
  vec2 f = fract(p);
  vec2 u = f * f * (3.0 - 2.0 * f);
  return mix(mix(hash(i), hash(i + vec2(1.0, 0.0)), u.x),
             mix(hash(i + vec2(0.0, 1.0)), hash(i + vec2(1.0, 1.0)), u.x), u.y);
}

float fbm(vec2 p) {
  float v = 0.0;
  float a = 0.5;
  for (int i = 0; i < 6; i++) {
    v += a * noise(p);
    p *= 2.02;
    a *= 0.5;
  }
  return v;
}

void main() {
  float aspect = uResolution.x / max(uResolution.y, 1.0);
  vec2 p = (vUv - 0.5) * vec2(aspect, 1.0) * 2.4;
  float t = uTime * 0.06;

  vec2 q = vec2(fbm(p + t), fbm(p - t + vec2(5.2, 1.3)));
  vec2 r = vec2(
    fbm(p + q + vec2(1.7, 9.2) + t * 0.5),
    fbm(p + q + vec2(8.3, 2.8) - t * 0.3)
  );
  float v = fbm(p + r * 1.5);

  vec3 silver = vec3(0.93, 0.94, 0.95);
  vec3 pearl  = vec3(0.80, 0.83, 0.89);
  vec3 col = mix(silver, pearl, clamp(v * 1.25, 0.0, 1.0));

  float ang = atan(r.y, r.x);
  col += 0.03 * sin(ang * 3.0 + uTime * 0.4 + v * 6.0);

  col = clamp(col, 0.82, 1.0);
  gl_FragColor = vec4(col, 1.0);
}
`

export function useSpiralVortex(canvasRef: Ref<HTMLCanvasElement | null>) {
  let rafId: number | null = null
  let gl: WebGLRenderingContext | null = null
  let program: WebGLProgram | null = null
  let posBuf: WebGLBuffer | null = null
  let uTime: WebGLUniformLocation | null = null
  let uResolution: WebGLUniformLocation | null = null
  let aPos = 0
  const start = performance.now()

  const compile = (type: number, src: string): WebGLShader | null => {
    const s = gl!.createShader(type)
    if (!s) return null
    gl!.shaderSource(s, src)
    gl!.compileShader(s)
    if (!gl!.getShaderParameter(s, gl!.COMPILE_STATUS)) {
      console.warn('[spiral-vortex] shader compile:', gl!.getShaderInfoLog(s))
      gl!.deleteShader(s)
      return null
    }
    return s
  }

  const resize = () => {
    const canvas = canvasRef.value
    if (!canvas || !gl) return
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    const w = Math.floor(window.innerWidth * dpr)
    const h = Math.floor(window.innerHeight * dpr)
    if (canvas.width !== w || canvas.height !== h) {
      canvas.width = w
      canvas.height = h
    }
    gl.viewport(0, 0, gl.drawingBufferWidth, gl.drawingBufferHeight)
  }

  const render = () => {
    if (!gl || !program) return
    resize()
    const t = (performance.now() - start) / 1000
    gl.uniform1f(uTime, t)
    gl.uniform2f(uResolution, gl.drawingBufferWidth, gl.drawingBufferHeight)
    gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4)
  }

  const loop = () => {
    render()
    rafId = requestAnimationFrame(loop)
  }

  onMounted(() => {
    const canvas = canvasRef.value
    if (!canvas) return
    const reduce = window.matchMedia('(prefers-reduced-motion: reduce)').matches

    gl = canvas.getContext('webgl', { antialias: true, alpha: false, premultipliedAlpha: false })
      || canvas.getContext('experimental-webgl') as WebGLRenderingContext | null
    if (!gl) {
      console.warn('[spiral-vortex] WebGL unavailable')
      return
    }

    const vs = compile(gl.VERTEX_SHADER, VERT)
    const fs = compile(gl.FRAGMENT_SHADER, FRAG)
    if (!vs || !fs) return

    program = gl.createProgram()
    if (!program) return
    gl.attachShader(program, vs)
    gl.attachShader(program, fs)
    gl.linkProgram(program)
    if (!gl.getProgramParameter(program, gl.LINK_STATUS)) {
      console.warn('[spiral-vortex] program link:', gl.getProgramInfoLog(program))
      return
    }
    gl.useProgram(program)

    posBuf = gl.createBuffer()
    gl.bindBuffer(gl.ARRAY_BUFFER, posBuf)
    gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, 1, 1]), gl.STATIC_DRAW)
    aPos = gl.getAttribLocation(program, 'aPos')
    gl.enableVertexAttribArray(aPos)
    gl.vertexAttribPointer(aPos, 2, gl.FLOAT, false, 0, 0)

    uTime = gl.getUniformLocation(program, 'uTime')
    uResolution = gl.getUniformLocation(program, 'uResolution')

    resize()
    render()
    if (!reduce) {
      window.addEventListener('resize', resize)
      rafId = requestAnimationFrame(loop)
    }
  })

  onUnmounted(() => {
    if (rafId) cancelAnimationFrame(rafId)
    window.removeEventListener('resize', resize)
    if (gl) {
      if (posBuf) gl.deleteBuffer(posBuf)
      if (program) gl.deleteProgram(program)
      gl.getExtension('WEBGL_lose_context')?.loseContext()
    }
    gl = null
    program = null
  })
}
