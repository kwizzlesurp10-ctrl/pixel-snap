import React, { useState, useEffect, useRef } from 'react';
import './index.css';

interface Snap {
  id: string;
  imageData: string;
  caption: string;
  tags: string[];
  timestamp: number;
  type: 'photo' | 'video' | 'memory';
  favorite: boolean;
  aiEnhanced?: string;
  duration?: number;
  title?: string;
  edits?: string[];
}

export default function App() {
  const [activeTab, setActiveTab] = useState<'camera' | 'gallery' | 'memories' | 'studio'>('camera');
  const [snaps, setSnaps] = useState<Snap[]>([]);
  const [captureMode, setCaptureMode] = useState<'photo' | 'video' | 'portrait'>('photo');
  const [isRecording, setIsRecording] = useState(false);
  const [recordTime, setRecordTime] = useState(0);
  const [flashOn, setFlashOn] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');
  const [favFilter, setFavFilter] = useState(false);
  const [aiMenuOpen, setAiMenuOpen] = useState(false);
  const [settingsOpen, setSettingsOpen] = useState(false);
  const [toasts, setToasts] = useState<{id: number, message: string}[]>([]);
  
  const videoRef = useRef<HTMLVideoElement>(null);
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const currentStream = useRef<MediaStream | null>(null);
  const mediaRecorder = useRef<MediaRecorder | null>(null);
  const recordedChunks = useRef<BlobPart[]>([]);
  const recordInterval = useRef<ReturnType<typeof setInterval> | null>(null);
  
  const [editorSnapId, setEditorSnapId] = useState<string | null>(null);
  const [brightness, setBrightness] = useState(0);
  const currentCanvasImage = useRef<HTMLImageElement | null>(null);

  useEffect(() => {
    try {
      const saved = localStorage.getItem('pixelsnap_react_snaps');
      if (saved) {
        setSnaps(JSON.parse(saved));
      } else {
        seedDemoData();
      }
    } catch (e) {
      seedDemoData();
    }
  }, []);

  const seedDemoData = () => {
    const demoImages = [
      "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='800' height='800'%3E%3Cdefs%3E%3ClinearGradient id='a' x1='0' y1='0' x2='1' y2='1'%3E%3Cstop offset='0%25' stop-color='%23111'/%3E%3Cstop offset='100%25' stop-color='%23222'/%3E%3C/linearGradient%3E%3C/defs%3E%3Crect width='800' height='800' fill='url(%23a)'/%3E%3Ccircle cx='280' cy='280' r='140' fill='%23333'/%3E%3Crect x='100' y='480' width='600' height='180' fill='%23222' opacity='0.7'/%3E%3C/svg%3E",
      "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='800' height='800'%3E%3Crect width='800' height='800' fill='%23222'/%3E%3Cpath d='M100 700 L300 200 L500 550 L700 150' fill='none' stroke='%23444' stroke-width='60'/%3E%3C/svg%3E"
    ];
    const now = Date.now();
    const initial: Snap[] = [
      { id: 'demo_1', imageData: demoImages[0], caption: "Golden evening light", tags: ['evening', 'pixel9'], timestamp: now - 3600000*3, type: 'photo', favorite: true, aiEnhanced: 'magic' },
      { id: 'demo_2', imageData: demoImages[1], caption: "Unexpected texture", tags: ['texture', 'best-take'], timestamp: now - 3600000*20, type: 'photo', favorite: false }
    ];
    setSnaps(initial);
    localStorage.setItem('pixelsnap_react_snaps', JSON.stringify(initial));
  };

  useEffect(() => {
    localStorage.setItem('pixelsnap_react_snaps', JSON.stringify(snaps));
  }, [snaps]);

  const showToast = (message: string) => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message }]);
    setTimeout(() => setToasts(prev => prev.filter(t => t.id !== id)), 2400);
  };

  const startCamera = async () => {
    if (currentStream.current) {
      currentStream.current.getTracks().forEach(t => t.stop());
    }
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { facingMode: 'environment', width: { ideal: 1280 }, height: { ideal: 720 } }
      });
      currentStream.current = stream;
      if (videoRef.current) {
        videoRef.current.srcObject = stream;
      }
    } catch (e) {
      console.warn("Camera access denied or unavailable.");
    }
  };

  const stopCamera = () => {
    if (currentStream.current) {
      currentStream.current.getTracks().forEach(t => t.stop());
      currentStream.current = null;
    }
    if (isRecording) stopRecording();
  };

  useEffect(() => {
    if (activeTab === 'camera') {
      startCamera();
    } else {
      stopCamera();
    }
    return () => stopCamera();
  }, [activeTab]);

  const capturePhoto = () => {
    if (!videoRef.current) return;
    const canvas = document.createElement('canvas');
    canvas.width = videoRef.current.videoWidth || 1280;
    canvas.height = videoRef.current.videoHeight || 720;
    const ctx = canvas.getContext('2d');
    if (!ctx) return;
    ctx.drawImage(videoRef.current, 0, 0, canvas.width, canvas.height);
    const imgData = canvas.toDataURL('image/jpeg', 0.95);
    
    const snap: Snap = {
      id: 'snap_' + Date.now(),
      imageData: imgData,
      caption: '',
      tags: [],
      timestamp: Date.now(),
      type: 'photo',
      favorite: false
    };
    setSnaps(prev => [snap, ...prev]);
    if (navigator.vibrate) navigator.vibrate([20]);
    showToast("Photo captured");
  };

  const startVideoRecording = () => {
    if (!currentStream.current) return;
    recordedChunks.current = [];
    try {
      const mr = new MediaRecorder(currentStream.current, { mimeType: 'video/webm;codecs=vp9' });
      mr.ondataavailable = e => { if (e.data.size > 0) recordedChunks.current.push(e.data); };
      mr.onstop = () => {
        const blob = new Blob(recordedChunks.current, { type: 'video/webm' });
        const url = URL.createObjectURL(blob);
        const snap: Snap = {
          id: 'snap_' + Date.now(), imageData: url, caption: '', tags: [], timestamp: Date.now(), type: 'video', favorite: false, duration: recordTime
        };
        setSnaps(prev => [snap, ...prev]);
        showToast("Video saved");
      };
      mr.start();
      mediaRecorder.current = mr;
      setIsRecording(true);
      setRecordTime(0);
      recordInterval.current = setInterval(() => setRecordTime(prev => prev + 1), 1000);
    } catch (e) {
      showToast("Recording not supported");
    }
  };

  const stopRecording = () => {
    if (mediaRecorder.current && isRecording) {
      mediaRecorder.current.stop();
      setIsRecording(false);
      if (recordInterval.current) clearInterval(recordInterval.current);
    }
  };

  const handleCaptureBtn = () => {
    if (captureMode === 'video') {
      isRecording ? stopRecording() : startVideoRecording();
    } else {
      capturePhoto();
    }
  };

  const toggleFavorite = (id: string, e?: React.MouseEvent) => {
    if(e) e.preventDefault();
    setSnaps(prev => prev.map(s => s.id === id ? { ...s, favorite: !s.favorite } : s));
  };

  const openDetail = (snap: Snap) => {
    if (snap.type === 'memory') {
      alert(`${snap.title}\n\n${snap.caption}`);
    } else {
      setEditorSnapId(snap.id);
      setActiveTab('studio');
    }
  };

  const filteredSnaps = snaps.filter(s => {
    if (favFilter && !s.favorite) return false;
    if (searchQuery) {
      const q = searchQuery.toLowerCase();
      if (!s.caption?.toLowerCase().includes(q) && !s.tags.some(t => t.toLowerCase().includes(q))) return false;
    }
    return true;
  });

  const generateAIMemory = (kind: string) => {
    if (snaps.length < 2) { showToast("Need more snaps!"); return; }
    let title = kind === 'week' ? "This Week on PixelSnap" : kind === 'story' ? "Golden Hour Story" : "Best of 2026";
    let body = "A beautiful AI-curated highlight reel of your moments.";
    const snap: Snap = {
      id: 'mem_' + Date.now(), imageData: snaps[0].imageData, caption: body, title, tags: ['ai-memory'], timestamp: Date.now(), type: 'memory', favorite: false
    };
    setSnaps(prev => [snap, ...prev]);
    showToast("AI Memory created!");
  };

  useEffect(() => {
    if (activeTab === 'studio' && editorSnapId) {
      const snap = snaps.find(s => s.id === editorSnapId);
      if (snap && canvasRef.current) {
        const ctx = canvasRef.current.getContext('2d');
        const img = new Image();
        img.onload = () => {
          if(!canvasRef.current) return;
          const scale = Math.min(800 / img.width, 800 / img.height);
          canvasRef.current.width = img.width * scale;
          canvasRef.current.height = img.height * scale;
          ctx?.drawImage(img, 0, 0, canvasRef.current.width, canvasRef.current.height);
          currentCanvasImage.current = img;
        };
        img.src = snap.imageData;
      }
    }
  }, [activeTab, editorSnapId]);

  const applyFilter = (filter: string) => {
    if (!canvasRef.current || !currentCanvasImage.current) return;
    const ctx = canvasRef.current.getContext('2d');
    if (!ctx) return;
    ctx.filter = 'none';
    if (filter === 'vibrant') ctx.filter = 'saturate(1.8) contrast(1.15)';
    else if (filter === 'warm') ctx.filter = 'sepia(0.4) saturate(1.3)';
    else if (filter === 'noir') ctx.filter = 'grayscale(1) contrast(1.3)';
    
    ctx.drawImage(currentCanvasImage.current, 0, 0, canvasRef.current.width, canvasRef.current.height);
  };

  const handleBrightness = (val: number) => {
    setBrightness(val);
    if (!canvasRef.current || !currentCanvasImage.current) return;
    const ctx = canvasRef.current.getContext('2d');
    if (!ctx) return;
    ctx.filter = `brightness(${1 + (val/100)})`;
    ctx.drawImage(currentCanvasImage.current, 0, 0, canvasRef.current.width, canvasRef.current.height);
  };

  const saveEdit = () => {
    if (!canvasRef.current || !editorSnapId) return;
    const data = canvasRef.current.toDataURL('image/jpeg', 0.95);
    setSnaps(prev => prev.map(s => s.id === editorSnapId ? { ...s, imageData: data, caption: s.caption + ' [Edited]' } : s));
    showToast("Edit saved");
    setActiveTab('gallery');
  };

  return (
    <div className="container">
      <div className="status-bar">
        <div>9:41</div>
        <div className="status-icons">
          <i className="fa-solid fa-signal"></i>
          <i className="fa-solid fa-wifi"></i>
          <i className="fa-solid fa-battery-three-quarters"></i>
        </div>
      </div>

      <div className="header">
        <div className="header-left">
          <img src="data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Crect width='100' height='100' rx='24' fill='%23111418'/%3E%3Ccircle cx='50' cy='50' r='26' fill='none' stroke='%2314b8a6' stroke-width='9'/%3E%3C/svg%3E" alt="icon" className="app-icon" />
          <div>
            <div className="app-title font-display">PixelSnap</div>
            <div className="app-subtitle">Full Experience <span className="version-badge">v2.0</span></div>
          </div>
        </div>
        <div className="header-right">
          <button className="icon-btn" onClick={() => setSettingsOpen(true)}><i className="fa-solid fa-cog"></i></button>
          <div className="device-badge"><div className="pulse-dot"></div> Pixel 9</div>
        </div>
      </div>

      <div className={`section ${activeTab === 'camera' ? 'active' : ''}`}>
        <div className="section-header">
          <div className="section-title">Capture</div>
          <div className="camera-modes">
            <button className={`mode-btn ${captureMode === 'photo' ? 'active' : ''}`} onClick={() => setCaptureMode('photo')}>Photo</button>
            <button className={`mode-btn ${captureMode === 'video' ? 'active' : ''}`} onClick={() => setCaptureMode('video')}>Video</button>
          </div>
        </div>
        <div className="camera-container">
          <video ref={videoRef} className="camera-feed" autoPlay playsInline muted style={{ filter: flashOn ? 'brightness(1.3)' : '' }} />
          <div className="camera-overlay"></div>
          <div className="camera-top-bar">
            <div className="camera-info-badge"><i className="fa-solid fa-video text-accent-1"></i> {captureMode === 'video' ? '4K60 HDR' : '50MP Tensor G4'}</div>
            <button className="icon-btn" onClick={() => setFlashOn(!flashOn)} style={{ color: flashOn ? '#facc15' : '' }}>
              <i className="fa-solid fa-bolt"></i>
            </button>
          </div>
          {isRecording && (
            <div style={{ position:'absolute', top: 16, right: 70, background: '#ef4444', color:'white', padding:'4px 10px', borderRadius:20, fontSize:12, fontWeight:'bold', zIndex: 10 }}>
              REC {Math.floor(recordTime/60)}:{(recordTime%60).toString().padStart(2,'0')}
            </div>
          )}
          <div className="camera-controls">
            <div className="camera-actions">
              <button className="btn-secondary" onClick={() => setActiveTab('gallery')}>Gallery</button>
              <div className={`capture-btn ${isRecording ? 'recording' : ''}`} onClick={handleCaptureBtn}>
                <div className="capture-inner"></div>
              </div>
              <button className="btn-primary" onClick={() => setAiMenuOpen(true)}><i className="fa-solid fa-magic"></i> Pixel AI</button>
            </div>
            <div className="mode-label">{captureMode.toUpperCase()} MODE • PIXEL 9 EXCLUSIVE</div>
          </div>
        </div>
      </div>

      <div className={`section ${activeTab === 'gallery' ? 'active' : ''}`}>
        <div className="section-header">
          <div>
            <div className="section-title">Gallery</div>
            <div className="gallery-count">{snaps.length} moments</div>
          </div>
        </div>
        <div className="search-bar">
          <input type="text" className="search-input" placeholder="Search moments, tags..." value={searchQuery} onChange={e => setSearchQuery(e.target.value)} />
          <button className={`fav-btn ${favFilter ? 'active' : ''}`} onClick={() => setFavFilter(!favFilter)}><i className="fa-solid fa-heart"></i></button>
        </div>
        <div className="snap-grid">
          {filteredSnaps.map(snap => (
            <div key={snap.id} className="snap-item" onClick={() => openDetail(snap)} onContextMenu={(e) => toggleFavorite(snap.id, e)}>
              {snap.type === 'video' ? (
                <video src={snap.imageData} className="snap-img" muted />
              ) : (
                <img src={snap.imageData} className="snap-img" alt="snap" />
              )}
              <div className="snap-overlay">
                <div className="snap-caption">{snap.caption || (snap.type === 'video' ? 'Video' : 'Moment')}</div>
              </div>
              {snap.favorite && <div className="snap-badge"><i className="fa-solid fa-heart"></i></div>}
            </div>
          ))}
        </div>
        {filteredSnaps.length === 0 && <div style={{textAlign:'center', marginTop: 60, color:'var(--text-muted)'}}>No moments found. Start capturing!</div>}
      </div>

      <div className={`section ${activeTab === 'memories' ? 'active' : ''}`}>
        <div className="section-header">
          <div>
            <div className="section-title">Memories</div>
            <div className="gallery-count">AI-curated stories</div>
          </div>
        </div>
        <div className="glass-card memory-card" onClick={() => generateAIMemory('week')}>
          <div className="memory-header">
            <div>
              <div className="memory-title text-gradient">This Week on Pixel 9</div>
              <div className="memory-subtitle">Auto-generated highlight reel</div>
            </div>
            <i className="fa-solid fa-magic text-accent-1 text-xl"></i>
          </div>
          <div className="memory-desc">A beautiful collection of your best moments from the past week, analyzed by Tensor G4.</div>
        </div>
        <div className="glass-card memory-card" onClick={() => generateAIMemory('story')}>
          <div className="memory-header">
            <div>
              <div className="memory-title text-gradient">Golden Hour Story</div>
              <div className="memory-subtitle">Narrative with AI voiceover</div>
            </div>
            <i className="fa-solid fa-book text-accent-2 text-xl"></i>
          </div>
          <div className="memory-desc">A poetic look at your evening snaps, stitched together into a cohesive visual story.</div>
        </div>
      </div>

      <div className={`section ${activeTab === 'studio' ? 'active' : ''}`}>
        <div className="section-header">
          <div className="section-title">Studio Editor</div>
          <button className="btn-secondary" style={{padding:'6px 12px'}} onClick={() => setActiveTab('gallery')}>Done</button>
        </div>
        <div className="canvas-container">
          <canvas ref={canvasRef}></canvas>
        </div>
        <div className="tool-group">
          <div className="tool-label">Filters</div>
          <div className="tool-grid">
            <button className="filter-btn" onClick={() => applyFilter('none')}>Original</button>
            <button className="filter-btn" onClick={() => applyFilter('vibrant')}>Vibrant</button>
            <button className="filter-btn" onClick={() => applyFilter('warm')}>Warm</button>
            <button className="filter-btn" onClick={() => applyFilter('noir')}>Noir</button>
          </div>
        </div>
        <div className="tool-group">
          <div className="tool-label" style={{display:'flex', justifyContent:'space-between'}}>
            <span>Brightness</span><span>{brightness}</span>
          </div>
          <input type="range" className="range-slider" min="-50" max="50" value={brightness} onChange={e => handleBrightness(Number(e.target.value))} />
        </div>
        <button className="btn-primary" style={{width:'100%', justifyContent:'center', marginTop: 20}} onClick={saveEdit}>Save Edit</button>
      </div>

      <div className="bottom-nav">
        <button className={`nav-item ${activeTab === 'camera' ? 'active' : ''}`} onClick={() => setActiveTab('camera')}>
          <i className="fa-solid fa-camera"></i> Camera
        </button>
        <button className={`nav-item ${activeTab === 'gallery' ? 'active' : ''}`} onClick={() => setActiveTab('gallery')}>
          <i className="fa-solid fa-images"></i> Gallery
        </button>
        <button className={`nav-item ${activeTab === 'memories' ? 'active' : ''}`} onClick={() => setActiveTab('memories')}>
          <i className="fa-solid fa-magic"></i> Memories
        </button>
        <button className={`nav-item ${activeTab === 'studio' ? 'active' : ''}`} onClick={() => setActiveTab('studio')}>
          <i className="fa-solid fa-palette"></i> Studio
        </button>
      </div>

      {aiMenuOpen && (
        <div className="modal-backdrop" onClick={() => setAiMenuOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title text-gradient">Pixel AI Tools</div>
              <button className="close-btn" onClick={() => setAiMenuOpen(false)}>&times;</button>
            </div>
            <div className="ai-grid">
              <div className="ai-option" onClick={() => { setAiMenuOpen(false); showToast("Magic Editor applied!"); }}>
                <div className="ai-option-title"><i className="fa-solid fa-wand-magic-sparkles text-accent-1"></i> Magic Editor</div>
                <div className="ai-option-desc">Remove objects, perfect lighting</div>
              </div>
              <div className="ai-option" onClick={() => { setAiMenuOpen(false); showToast("Best Take selected!"); }}>
                <div className="ai-option-title"><i className="fa-solid fa-star text-accent-2"></i> Best Take</div>
                <div className="ai-option-desc">Pick the perfect frame</div>
              </div>
            </div>
          </div>
        </div>
      )}

      {settingsOpen && (
        <div className="modal-backdrop" onClick={() => setSettingsOpen(false)}>
          <div className="modal-content" onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div className="modal-title">Settings</div>
              <button className="close-btn" onClick={() => setSettingsOpen(false)}>&times;</button>
            </div>
            <div style={{display:'flex',flexDirection:'column',gap:16, fontSize:14}}>
              <div style={{display:'flex',justifyContent:'space-between'}}>
                <span>Dynamic Theme</span><span style={{color:'var(--accent-1)'}}>ON</span>
              </div>
              <div style={{display:'flex',justifyContent:'space-between'}}>
                <span>AI Intensity</span><span style={{color:'var(--text-muted)'}}>Maximum</span>
              </div>
              <div style={{display:'flex',justifyContent:'space-between'}}>
                <span>Auto Backup</span><span style={{color:'var(--accent-1)'}}>ON</span>
              </div>
            </div>
          </div>
        </div>
      )}

      <div className="toast-container">
        {toasts.map(t => (
          <div key={t.id} className="toast">{t.message}</div>
        ))}
      </div>
    </div>
  );
}
