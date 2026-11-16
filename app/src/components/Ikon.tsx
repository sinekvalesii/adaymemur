import Svg, { Circle, Path, Rect } from 'react-native-svg';

export type IkonAdi =
  | 'liste' | 'hedef' | 'yerimi' | 'yerimiDolu' | 'sohbet' | 'kisi' | 'filtre' | 'ara' | 'zil'
  | 'takvim' | 'dis' | 'paylas' | 'tik' | 'carpi' | 'sag' | 'sol' | 'kilit' | 'bilgi' | 'konum'
  | 'saat' | 'arti' | 'sil' | 'bayrak' | 'cikis' | 'kalkan' | 'belge' | 'yildiz' | 'uyari' | 'kalem'
  | 'yenile' | 'engel' | 'okul' | 'grafik';

/** El çizimi, 24×24, 1.75 çizgi kalınlığında ikon seti. */
export function Ikon({ ad, boyut = 22, renk = '#000', kalinlik = 1.75 }: { ad: IkonAdi; boyut?: number; renk?: string; kalinlik?: number }) {
  const p = { stroke: renk, strokeWidth: kalinlik, strokeLinecap: 'round' as const, strokeLinejoin: 'round' as const, fill: 'none' };
  const c = (() => {
    switch (ad) {
      case 'liste':
        return (<>
          <Circle cx={5} cy={6.5} r={1.6} {...p} /><Path d="M9.5 6.5H20" {...p} />
          <Circle cx={5} cy={12} r={1.6} {...p} /><Path d="M9.5 12H20" {...p} />
          <Circle cx={5} cy={17.5} r={1.6} {...p} /><Path d="M9.5 17.5H16" {...p} />
        </>);
      case 'hedef':
        return (<>
          <Circle cx={12} cy={12} r={8.5} {...p} /><Circle cx={12} cy={12} r={4.5} {...p} />
          <Circle cx={12} cy={12} r={1.2} fill={renk} />
        </>);
      case 'yerimi':
        return <Path d="M6.5 4.5h11v15.5l-5.5-3.6-5.5 3.6z" {...p} />;
      case 'yerimiDolu':
        return <Path d="M6.5 4.5h11v15.5l-5.5-3.6-5.5 3.6z" {...p} fill={renk} />;
      case 'sohbet':
        return <Path d="M4.5 6.5a2 2 0 0 1 2-2h11a2 2 0 0 1 2 2v8a2 2 0 0 1-2 2H11l-4.2 3.3V16.5h-.3a2 2 0 0 1-2-2z" {...p} />;
      case 'kisi':
        return (<><Circle cx={12} cy={8.5} r={3.8} {...p} /><Path d="M4.8 20c.9-3.6 3.8-5.6 7.2-5.6s6.3 2 7.2 5.6" {...p} /></>);
      case 'filtre':
        return (<>
          <Path d="M4 7h9M17 7h3M4 17h3M11 17h9" {...p} />
          <Circle cx={15} cy={7} r={2} {...p} /><Circle cx={9} cy={17} r={2} {...p} />
        </>);
      case 'ara':
        return (<><Circle cx={10.5} cy={10.5} r={6} {...p} /><Path d="M15 15l4.8 4.8" {...p} /></>);
      case 'zil':
        return <Path d="M6.5 16.5V11a5.5 5.5 0 0 1 11 0v5.5l1.5 1.5H5zM10 20.5h4" {...p} />;
      case 'takvim':
        return (<>
          <Rect x={4} y={5.5} width={16} height={14.5} rx={2.2} {...p} />
          <Path d="M4 10h16M8.5 3.5v3.5M15.5 3.5v3.5" {...p} />
        </>);
      case 'dis':
        return <Path d="M13.5 4.5h6v6M19.5 4.5l-8 8M17.5 13.5v4a2 2 0 0 1-2 2h-9a2 2 0 0 1-2-2v-9a2 2 0 0 1 2-2h4" {...p} />;
      case 'paylas':
        return <Path d="M12 3.8v11M7.8 8 12 3.8 16.2 8M5 13v5.2a2 2 0 0 0 2 2h10a2 2 0 0 0 2-2V13" {...p} />;
      case 'tik':
        return <Path d="M5 12.5l4.2 4.2L19 7" {...p} />;
      case 'carpi':
        return <Path d="M6.5 6.5l11 11M17.5 6.5l-11 11" {...p} />;
      case 'sag':
        return <Path d="M9.5 5.5 16 12l-6.5 6.5" {...p} />;
      case 'sol':
        return <Path d="M14.5 5.5 8 12l6.5 6.5" {...p} />;
      case 'kilit':
        return (<><Rect x={5} y={10.5} width={14} height={10} rx={2.2} {...p} /><Path d="M8.5 10.5V8a3.5 3.5 0 0 1 7 0v2.5" {...p} /></>);
      case 'bilgi':
        return (<><Circle cx={12} cy={12} r={8.5} {...p} /><Path d="M12 11v5.2" {...p} /><Circle cx={12} cy={7.9} r={0.9} fill={renk} /></>);
      case 'konum':
        return (<><Path d="M12 20.5s-6.5-5.6-6.5-10.6a6.5 6.5 0 0 1 13 0c0 5-6.5 10.6-6.5 10.6z" {...p} /><Circle cx={12} cy={10} r={2.3} {...p} /></>);
      case 'saat':
        return (<><Circle cx={12} cy={12} r={8.5} {...p} /><Path d="M12 7.5V12l3 2" {...p} /></>);
      case 'arti':
        return <Path d="M12 5v14M5 12h14" {...p} />;
      case 'sil':
        return <Path d="M5 7h14M10 4.5h4M7 7l.8 12a2 2 0 0 0 2 1.8h4.4a2 2 0 0 0 2-1.8L17 7" {...p} />;
      case 'bayrak':
        return <Path d="M6 20.5V4.5M6 5h10.5l-2 3.8 2 3.7H6" {...p} />;
      case 'cikis':
        return <Path d="M14 4.5h3.5a2 2 0 0 1 2 2v11a2 2 0 0 1-2 2H14M10.5 8 6.5 12l4 4M6.5 12h9" {...p} />;
      case 'kalkan':
        return <Path d="M12 3.8 5.5 6.2v5.3c0 4.2 2.8 7.4 6.5 8.7 3.7-1.3 6.5-4.5 6.5-8.7V6.2z" {...p} />;
      case 'belge':
        return <Path d="M7 3.8h7l4 4v11.4a1.8 1.8 0 0 1-1.8 1.8H7a1.8 1.8 0 0 1-1.8-1.8V5.6A1.8 1.8 0 0 1 7 3.8zM13.5 4v4.3H18M8.5 13h7M8.5 16.5h5" {...p} />;
      case 'yildiz':
        return <Path d="M12 3.5l1.9 5.6 5.6 1.9-5.6 1.9L12 18.5l-1.9-5.6L4.5 11l5.6-1.9z" {...p} />;
      case 'uyari':
        return (<><Path d="M10.3 4.6 3.4 17a2 2 0 0 0 1.7 3h13.8a2 2 0 0 0 1.7-3L13.7 4.6a2 2 0 0 0-3.4 0z" {...p} /><Path d="M12 9.5v4" {...p} /><Circle cx={12} cy={16.6} r={0.9} fill={renk} /></>);
      case 'kalem':
        return <Path d="M4.5 19.5l1-4.2L15.8 5a2 2 0 0 1 2.9 0l.3.3a2 2 0 0 1 0 2.9L8.7 18.5zM13.8 7l3.2 3.2" {...p} />;
      case 'yenile':
        return <Path d="M19 12a7 7 0 1 1-2.1-5M19 4.5V8h-3.5" {...p} />;
      case 'engel':
        return (<><Circle cx={12} cy={12} r={8.5} {...p} /><Path d="M6 6l12 12" {...p} /></>);
      case 'okul':
        return <Path d="M3.5 9.5 12 5l8.5 4.5L12 14zM7 11.5v4.3c1.4 1.4 3.2 2.2 5 2.2s3.6-.8 5-2.2v-4.3M20.5 9.5v5" {...p} />;
      case 'grafik':
        return <Path d="M4.5 19.5h15M7 16v-4M11.5 16V8M16 16v-6.5" {...p} />;
    }
  })();
  return (
    <Svg width={boyut} height={boyut} viewBox="0 0 24 24">
      {c}
    </Svg>
  );
}
