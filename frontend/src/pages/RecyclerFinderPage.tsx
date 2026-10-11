import React, { useEffect, useState, useRef } from 'react';
import { recyclerService } from '../services';
import { RecyclerItem } from '../types';
import { Button } from '../components/ui/Button';
import { Skeleton } from '../components/ui/Skeleton';
import { EmptyState } from '../components/ui/EmptyState';
import L from 'leaflet';

export const RecyclerFinderPage: React.FC = () => {
  const [recyclers, setRecyclers] = useState<RecyclerItem[]>([]);
  const [filteredRecyclers, setFilteredRecyclers] = useState<RecyclerItem[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  // Filters
  const [selectedState, setSelectedState] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [viewMode, setViewMode] = useState<'map' | 'list'>('map');

  // Leaflet Map Ref
  const mapContainerRef = useRef<HTMLDivElement>(null);
  const mapInstanceRef = useRef<L.Map | null>(null);
  const markersRef = useRef<L.LayerGroup | null>(null);

  useEffect(() => {
    fetchRecyclers();
  }, []);

  useEffect(() => {
    filterData();
  }, [recyclers, selectedState, selectedCategory, searchQuery]);

  useEffect(() => {
    if (viewMode === 'map' && mapContainerRef.current) {
      if (!mapInstanceRef.current) {
        // Initialize Leaflet map centered over India
        const map = L.map(mapContainerRef.current).setView([20.5937, 78.9629], 5);
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
          attribution: '&copy; OpenStreetMap contributors',
        }).addTo(map);

        const markersGroup = L.layerGroup().addTo(map);
        markersRef.current = markersGroup;
        mapInstanceRef.current = map;
      }

      updateMapMarkers();
    }
  }, [viewMode, filteredRecyclers]);

  const fetchRecyclers = async () => {
    try {
      setIsLoading(true);
      const res = await recyclerService.getRecyclers();
      setRecyclers(res);
    } catch {
      // Handled
    } finally {
      setIsLoading(false);
    }
  };

  const filterData = () => {
    let result = [...recyclers];

    if (selectedState) {
      result = result.filter((r) => r.state.toLowerCase() === selectedState.toLowerCase());
    }
    if (selectedCategory) {
      result = result.filter(
        (r) => r.categoriesHandled && r.categoriesHandled.includes(selectedCategory)
      );
    }
    if (searchQuery.trim()) {
      const q = searchQuery.toLowerCase();
      result = result.filter(
        (r) =>
          r.name.toLowerCase().includes(q) ||
          r.city.toLowerCase().includes(q) ||
          r.state.toLowerCase().includes(q)
      );
    }

    setFilteredRecyclers(result);
  };

  const updateMapMarkers = () => {
    if (!markersRef.current || !mapInstanceRef.current) return;
    markersRef.current.clearLayers();

    filteredRecyclers.forEach((r) => {
      if (r.latitude && r.longitude) {
        const marker = L.circleMarker([r.latitude, r.longitude], {
          radius: 8,
          fillColor: '#3FB950',
          color: '#ffffff',
          weight: 2,
          opacity: 1,
          fillOpacity: 0.8,
        });

        const popupContent = `
          <div style="font-family: 'IBM Plex Sans', sans-serif; color: #161B22; padding: 4px; min-width: 200px;">
            <div style="font-weight: 700; font-size: 13px; margin-bottom: 2px;">${r.name}</div>
            <div style="font-size: 11px; color: #484F58; margin-bottom: 6px;">${r.city}, ${r.state}</div>
            <div style="font-size: 11px; margin-bottom: 2px;"><strong>Capacity:</strong> ${r.formattedCapacity}</div>
            <div style="font-size: 11px; margin-bottom: 2px;"><strong>Contact:</strong> ${r.contactPhone}</div>
            <div style="font-size: 11px; margin-bottom: 6px;"><strong>Email:</strong> ${r.contactEmail}</div>
            <div style="display: inline-block; background-color: #3FB950; color: #ffffff; padding: 2px 6px; border-radius: 4px; font-size: 10px; font-weight: 600;">
              CPCB Certified
            </div>
          </div>
        `;

        marker.bindPopup(popupContent);
        markersRef.current?.addLayer(marker);
      }
    });
  };

  const states = Array.from(new Set(recyclers.map((r) => r.state))).sort();

  return (
    <div className="p-4 md:p-6 flex flex-col gap-6">
      {/* Header & Controls */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h2 className="text-xl font-bold text-text">CPCB Authorized Recycler Directory</h2>
          <p className="text-xs text-text-muted mt-0.5">
            Rule 5: Procurement of certified recycling capacity across India
          </p>
        </div>

        {/* View Toggle */}
        <div className="flex bg-surface-2 p-1 border border-border rounded-input w-fit">
          <button
            onClick={() => setViewMode('map')}
            className={`px-3 py-1 text-xs font-semibold rounded transition-colors ${
              viewMode === 'map' ? 'bg-blue text-bg' : 'text-text-muted hover:text-text'
            }`}
          >
            🗺️ Map View
          </button>
          <button
            onClick={() => setViewMode('list')}
            className={`px-3 py-1 text-xs font-semibold rounded transition-colors ${
              viewMode === 'list' ? 'bg-blue text-bg' : 'text-text-muted hover:text-text'
            }`}
          >
            📋 List View
          </button>
        </div>
      </div>

      {/* Filter Toolbar */}
      <div className="p-4 bg-surface border border-border rounded-card flex flex-wrap gap-4 items-center">
        {/* Search Input */}
        <div className="flex-1 min-w-[200px]">
          <input
            type="text"
            placeholder="Search by city, state or facility name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-surface-2 border border-border rounded-input px-3 py-2 text-xs text-text placeholder-text-dim focus:outline-none focus:border-blue"
          />
        </div>

        {/* State filter */}
        <select
          value={selectedState}
          onChange={(e) => setSelectedState(e.target.value)}
          className="bg-surface-2 border border-border text-xs rounded-input px-3 py-2 text-text focus:outline-none focus:border-blue min-w-[140px]"
        >
          <option value="">All States</option>
          {states.map((st) => (
            <option key={st} value={st}>
              {st}
            </option>
          ))}
        </select>

        {/* Category filter */}
        <select
          value={selectedCategory}
          onChange={(e) => setSelectedCategory(e.target.value)}
          className="bg-surface-2 border border-border text-xs rounded-input px-3 py-2 text-text focus:outline-none focus:border-blue min-w-[160px]"
        >
          <option value="">All Categories</option>
          <option value="MOBILE_PHONE">Mobile Phones</option>
          <option value="LAPTOP_COMPUTER">Laptop Computers</option>
          <option value="AIR_CONDITIONER">Air Conditioners</option>
          <option value="EARPHONE_HEADPHONE">Earphones & Audio</option>
          <option value="WASHING_MACHINE">Washing Machines</option>
          <option value="REFRIGERATOR">Refrigerators</option>
          <option value="SOLAR_PANEL_INVERTER">Solar Inverters</option>
        </select>

        {(selectedState || selectedCategory || searchQuery) && (
          <Button
            variant="ghost"
            size="sm"
            onClick={() => {
              setSelectedState('');
              setSelectedCategory('');
              setSearchQuery('');
            }}
          >
            Clear Filters
          </Button>
        )}
      </div>

      {/* Main Content Area */}
      {isLoading ? (
        <div className="flex flex-col gap-3">
          <Skeleton className="h-96 w-full rounded-card" />
        </div>
      ) : filteredRecyclers.length === 0 ? (
        <EmptyState
          title="No Recyclers Found"
          description="No registered recycling facilities match your current state or product category filters."
          actionText="Reset All Filters"
          onAction={() => {
            setSelectedState('');
            setSelectedCategory('');
            setSearchQuery('');
          }}
        />
      ) : viewMode === 'map' ? (
        <div className="bg-surface border border-border rounded-card overflow-hidden shadow-2xl relative">
          <div ref={mapContainerRef} className="w-full h-[520px] z-10" />
          <div className="p-3 bg-surface border-t border-border flex items-center justify-between text-xs text-text-muted">
            <span>Showing {filteredRecyclers.length} facilities across India</span>
            <span className="flex items-center gap-2">
              <span className="w-2.5 h-2.5 rounded-full bg-green" /> CPCB Certified Facilities
            </span>
          </div>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {filteredRecyclers.map((r) => (
            <div
              key={r.recyclerId}
              className="p-5 bg-surface border border-border rounded-card flex flex-col justify-between hover:border-text-dim transition-colors"
            >
              <div>
                <div className="flex items-start justify-between gap-2">
                  <h4 className="text-sm font-semibold text-text">{r.name}</h4>
                  <span className="text-[10px] font-mono font-semibold px-2 py-0.5 rounded bg-green/15 text-green border border-green/30 shrink-0">
                    CPCB Certified
                  </span>
                </div>
                <p className="text-xs text-text-muted mt-1 font-mono">
                  {r.city}, {r.state}
                </p>

                <div className="mt-4 flex flex-col gap-1.5 text-xs">
                  <div className="flex justify-between text-text-dim">
                    <span>Capacity:</span>
                    <span className="font-mono text-text font-medium">{r.formattedCapacity}</span>
                  </div>
                  <div className="flex justify-between text-text-dim">
                    <span>Phone:</span>
                    <span className="font-mono text-text">{r.contactPhone}</span>
                  </div>
                  <div className="flex justify-between text-text-dim">
                    <span>Email:</span>
                    <span className="text-blue truncate max-w-[160px]">{r.contactEmail}</span>
                  </div>
                </div>

                <div className="mt-4 flex flex-wrap gap-1">
                  {r.categoriesHandled?.map((cat) => (
                    <span
                      key={cat}
                      className="text-[10px] bg-surface-2 border border-border px-1.5 py-0.5 rounded text-text-muted"
                    >
                      {cat.replace(/_/g, ' ')}
                    </span>
                  ))}
                </div>
              </div>

              <div className="mt-5 pt-4 border-t border-border flex justify-end">
                <Button
                  variant="secondary"
                  size="sm"
                  onClick={() => alert(`Connect with ${r.name}: ${r.contactEmail}`)}
                >
                  Contact Recycler
                </Button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
