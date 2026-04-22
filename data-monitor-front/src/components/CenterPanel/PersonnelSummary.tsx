import type { PersonnelSummary as PersonnelSummaryType } from '../../types/dashboard';
import GlowingPedestalStat from './GlowingPedestalStat';

interface Props {
  data: PersonnelSummaryType;
}

export default function PersonnelSummary({ data }: Props) {
  return (
    <div style={{ display: 'flex', gap: '10px', marginBottom: '6px' }}>
      <GlowingPedestalStat value={data.totalHeadcount} label="总人数" unit="人" />
      <GlowingPedestalStat value={data.internalStaff} label="自有人员" unit="人" />
      <GlowingPedestalStat value={data.outsourcedStaff} label="外协人员" unit="人" />
      <GlowingPedestalStat value={data.monthlyInvestment} label="本月投入" />
    </div>
  );
}
