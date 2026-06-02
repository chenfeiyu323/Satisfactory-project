import type { ExternalConnection, ExternalSourceOption, MaterialType, Recipe, RecipeMaterialAmount, TransportAdvice, TransportType } from './types';

export const materialTypeLabels: Record<MaterialType, string> = {
  SOLID: '固体',
  FLUID: '液体',
  GAS: '气体',
};

export const transportTypeLabels: Record<TransportType, string> = {
  BELT: '传送带',
  PIPE: '管道',
};

const materialByKey: Record<string, string> = {
  iron_ore: '铁矿石',
  copper_ore: '铜矿石',
  limestone: '石灰岩',
  coal: '煤',
  caterium_ore: '钦金矿石',
  raw_quartz: '粗石英',
  sulfur: '硫',
  bauxite: '铝土矿',
  uranium: '铀',
  sam: 'SAM',
  crude_oil: '原油',
  water: '水',
  nitrogen_gas: '氮气',
  iron_ingot: '铁锭',
  iron_plate: '铁板',
  iron_rod: '铁棒',
  screw: '螺丝',
  copper_ingot: '铜锭',
  copper_sheet: '铜板',
  wire: '电线',
  cable: '电缆',
  concrete: '混凝土',
  reinforced_iron_plate: '加强铁板',
  rotor: '转子',
  modular_frame: '模块化框架',
  steel_ingot: '钢锭',
  steel_beam: '钢梁',
  steel_pipe: '钢管',
  encased_industrial_beam: '封装工业梁',
  stator: '定子',
  motor: '电机',
  heavy_modular_frame: '重型模块化框架',
  caterium_ingot: '钦金锭',
  quickwire: '快速线',
  ai_limiter: 'AI 限制器',
  high_speed_connector: '高速连接器',
  quartz_crystal: '石英晶体',
  silica: '二氧化硅',
  crystal_oscillator: '晶体振荡器',
  circuit_board: '电路板',
  computer: '电脑',
  supercomputer: '超级计算机',
  plastic: '塑料',
  rubber: '橡胶',
  polymer_resin: '聚合树脂',
  petroleum_coke: '石油焦',
  compacted_coal: '压实煤',
  black_powder: '黑火药',
  smokeless_powder: '无烟火药',
  heavy_oil_residue: '重油残渣',
  fuel: '燃油',
  turbofuel: '涡轮燃油',
  rocket_fuel: '火箭燃料',
  sulfuric_acid: '硫酸',
  nitric_acid: '硝酸',
  ionized_fuel: '电离燃料',
  alumina_solution: '氧化铝溶液',
  aluminum_scrap: '铝废料',
  aluminum_ingot: '铝锭',
  alclad_aluminum_sheet: '覆铝板',
  aluminum_casing: '铝制外壳',
  heat_sink: '散热器',
  cooling_system: '冷却系统',
  battery: '电池',
  radio_control_unit: '无线电控制单元',
  fused_modular_frame: '熔合模块化框架',
  turbo_motor: '涡轮电机',
  electromagnetic_control_rod: '电磁控制棒',
  uranium_fuel_rod: '铀燃料棒',
  uranium_waste: '铀废料',
  non_fissile_uranium: '非裂变铀',
  plutonium_pellet: '钚颗粒',
  encased_plutonium_cell: '封装钚电池',
  plutonium_fuel_rod: '钚燃料棒',
  plutonium_waste: '钚废料',
  copper_powder: '铜粉',
  pressure_conversion_cube: '压力转换立方体',
  nuclear_pasta: '核意面',
  diamonds: '钻石',
  time_crystal: '时间晶体',
  dark_matter_residue: '暗物质残渣',
  dark_matter_crystal: '暗物质晶体',
  excited_photonic_matter: '激发态光子物质',
  reanimated_sam: '复活 SAM',
  sam_fluctuator: 'SAM 波动器',
  ficsonium: 'Ficsonium',
  ficsonium_fuel_rod: 'Ficsonium 燃料棒',
  empty_canister: '空桶',
  empty_fluid_tank: '空流体罐',
  packaged_water: '封装水',
  packaged_oil: '封装原油',
  packaged_fuel: '封装燃油',
  packaged_heavy_oil_residue: '封装重油残渣',
  packaged_turbofuel: '封装涡轮燃油',
  packaged_rocket_fuel: '封装火箭燃料',
  packaged_alumina_solution: '封装氧化铝溶液',
  packaged_sulfuric_acid: '封装硫酸',
  packaged_nitric_acid: '封装硝酸',
  packaged_nitrogen_gas: '封装氮气',
  packaged_ionized_fuel: '封装电离燃料',
  smart_plating: '智能嵌板',
  versatile_framework: '多用途框架',
  automated_wiring: '自动化线缆',
  modular_engine: '模块化发动机',
  adaptive_control_unit: '自适应控制单元',
  assembly_director_system: '装配主管系统',
  magnetic_field_generator: '磁场发生器',
  thermal_propulsion_rocket: '热推进火箭',
  ai_expansion_server: 'AI 扩展服务器',
  biochemical_sculptor: '生化雕刻器',
  ballistic_warp_drive: '弹道跃迁驱动器',
  portable_miner: '便携式采矿机',
  power_shard: '能量碎片',
  blue_power_slug: '蓝色能量蛞蝓',
  yellow_power_slug: '黄色能量蛞蝓',
  purple_power_slug: '紫色能量蛞蝓',
  solid_biofuel: '固体生物燃料',
  biomass: '生物质',
  wood: '木材',
  leaves: '树叶',
  mycelia: '菌丝',
  fabric: '织物',
  uranium_pellet: '铀颗粒',
  encased_uranium_cell: '封装铀电池',
};

const materialByEnglish: Record<string, string> = {
  'Iron Ore': '铁矿石',
  'Copper Ore': '铜矿石',
  Limestone: '石灰岩',
  Coal: '煤',
  'Caterium Ore': '钦金矿石',
  'Raw Quartz': '粗石英',
  Sulfur: '硫',
  Bauxite: '铝土矿',
  Uranium: '铀',
  SAM: 'SAM',
  'Crude Oil': '原油',
  Water: '水',
  'Nitrogen Gas': '氮气',
  'Iron Ingot': '铁锭',
  'Iron Plate': '铁板',
  'Iron Rod': '铁棒',
  Screw: '螺丝',
  'Copper Ingot': '铜锭',
  'Copper Sheet': '铜板',
  Wire: '电线',
  Cable: '电缆',
  Concrete: '混凝土',
  'Reinforced Iron Plate': '加强铁板',
  Rotor: '转子',
  'Modular Frame': '模块化框架',
  'Steel Ingot': '钢锭',
  'Steel Beam': '钢梁',
  'Steel Pipe': '钢管',
  'Encased Industrial Beam': '封装工业梁',
  Stator: '定子',
  Motor: '电机',
  'Heavy Modular Frame': '重型模块化框架',
  'Caterium Ingot': '钦金锭',
  Quickwire: '快速线',
  'AI Limiter': 'AI 限制器',
  'High-Speed Connector': '高速连接器',
  'Quartz Crystal': '石英晶体',
  Silica: '二氧化硅',
  'Crystal Oscillator': '晶体振荡器',
  'Circuit Board': '电路板',
  Computer: '电脑',
  Supercomputer: '超级计算机',
  Plastic: '塑料',
  Rubber: '橡胶',
  'Polymer Resin': '聚合树脂',
  'Petroleum Coke': '石油焦',
  'Compacted Coal': '压实煤',
  'Black Powder': '黑火药',
  'Smokeless Powder': '无烟火药',
  'Heavy Oil Residue': '重油残渣',
  Fuel: '燃油',
  Turbofuel: '涡轮燃油',
  'Rocket Fuel': '火箭燃料',
  'Sulfuric Acid': '硫酸',
  'Nitric Acid': '硝酸',
  'Ionized Fuel': '电离燃料',
  'Alumina Solution': '氧化铝溶液',
  'Aluminum Scrap': '铝废料',
  'Aluminum Ingot': '铝锭',
  'Alclad Aluminum Sheet': '覆铝板',
  'Aluminum Casing': '铝制外壳',
  'Heat Sink': '散热器',
  'Cooling System': '冷却系统',
  Battery: '电池',
  'Radio Control Unit': '无线电控制单元',
  'Fused Modular Frame': '熔合模块化框架',
  'Turbo Motor': '涡轮电机',
  'Electromagnetic Control Rod': '电磁控制棒',
  'Uranium Fuel Rod': '铀燃料棒',
  'Uranium Waste': '铀废料',
  'Non-Fissile Uranium': '非裂变铀',
  'Plutonium Pellet': '钚颗粒',
  'Encased Plutonium Cell': '封装钚电池',
  'Plutonium Fuel Rod': '钚燃料棒',
  'Plutonium Waste': '钚废料',
  'Copper Powder': '铜粉',
  'Pressure Conversion Cube': '压力转换立方体',
  'Nuclear Pasta': '核意面',
  Diamonds: '钻石',
  'Time Crystal': '时间晶体',
  'Dark Matter Residue': '暗物质残渣',
  'Dark Matter Crystal': '暗物质晶体',
  'Excited Photonic Matter': '激发态光子物质',
  'Reanimated SAM': '复活 SAM',
  'SAM Fluctuator': 'SAM 波动器',
  Ficsonium: 'Ficsonium',
  'Ficsonium Fuel Rod': 'Ficsonium 燃料棒',
  'Empty Canister': '空桶',
  'Empty Fluid Tank': '空流体罐',
  'Packaged Water': '封装水',
  'Packaged Oil': '封装原油',
  'Packaged Fuel': '封装燃油',
  'Packaged Heavy Oil Residue': '封装重油残渣',
  'Packaged Turbofuel': '封装涡轮燃油',
  'Packaged Rocket Fuel': '封装火箭燃料',
  'Packaged Alumina Solution': '封装氧化铝溶液',
  'Packaged Sulfuric Acid': '封装硫酸',
  'Packaged Nitric Acid': '封装硝酸',
  'Packaged Nitrogen Gas': '封装氮气',
  'Packaged Ionized Fuel': '封装电离燃料',
  'Smart Plating': '智能嵌板',
  'Versatile Framework': '多用途框架',
  'Automated Wiring': '自动化线缆',
  'Modular Engine': '模块化发动机',
  'Adaptive Control Unit': '自适应控制单元',
  'Assembly Director System': '装配主管系统',
  'Magnetic Field Generator': '磁场发生器',
  'Thermal Propulsion Rocket': '热推进火箭',
  'AI Expansion Server': 'AI 扩展服务器',
  'Biochemical Sculptor': '生化雕刻器',
  'Ballistic Warp Drive': '弹道跃迁驱动器',
  'Portable Miner': '便携式采矿机',
  'Power Shard': '能量碎片',
  'Blue Power Slug': '蓝色能量蛞蝓',
  'Yellow Power Slug': '黄色能量蛞蝓',
  'Purple Power Slug': '紫色能量蛞蝓',
  'Solid Biofuel': '固体生物燃料',
  Biomass: '生物质',
  Wood: '木材',
  Leaves: '树叶',
  Mycelia: '菌丝',
  Fabric: '织物',
  'Uranium Pellet': '铀颗粒',
  'Encased Uranium Cell': '封装铀电池',
};

const machineByEnglish: Record<string, string> = {
  Miner: '采矿机',
  'Water Extractor': '抽水机',
  'Oil Extractor': '采油机',
  'Resource Well Extractor': '资源井采集器',
  Smelter: '熔炉',
  Foundry: '铸造厂',
  Constructor: '构造器',
  Assembler: '装配站',
  Manufacturer: '制造站',
  Refinery: '精炼厂',
  Packager: '封装机',
  Blender: '搅拌机',
  'Particle Accelerator': '粒子加速器',
  Converter: '转换器',
  'Quantum Encoder': '量子编码器',
};

const recipeByKey: Record<string, string> = {
  recipe_extract_iron_ore_mk1_normal: '铁矿石开采 Mk.1 普通矿',
  recipe_extract_copper_ore_mk1_normal: '铜矿石开采 Mk.1 普通矿',
  recipe_extract_limestone_mk1_normal: '石灰岩开采 Mk.1 普通矿',
  recipe_extract_coal_mk1_normal: '煤开采 Mk.1 普通矿',
  recipe_extract_water: '抽取水',
  recipe_extract_crude_oil_normal: '原油开采 普通油井',
  recipe_iron_ingot: '铁锭',
  recipe_copper_ingot: '铜锭',
  recipe_concrete: '混凝土',
  recipe_caterium_ingot: '钦金锭',
  recipe_quartz_crystal: '石英晶体',
  recipe_silica: '二氧化硅',
  recipe_iron_plate: '铁板',
  recipe_iron_rod: '铁棒',
  recipe_screw: '螺丝',
  recipe_wire: '电线',
  recipe_cable: '电缆',
  recipe_copper_sheet: '铜板',
  recipe_reinforced_iron_plate: '加强铁板',
  recipe_rotor: '转子',
  recipe_modular_frame: '模块化框架',
  recipe_steel_ingot: '钢锭',
  recipe_steel_beam: '钢梁',
  recipe_steel_pipe: '钢管',
  recipe_encased_industrial_beam: '封装工业梁',
  recipe_stator: '定子',
  recipe_motor: '电机',
  recipe_heavy_modular_frame: '重型模块化框架',
  recipe_quickwire: '快速线',
  recipe_ai_limiter: 'AI 限制器',
  recipe_high_speed_connector: '高速连接器',
  recipe_circuit_board: '电路板',
  recipe_computer: '电脑',
  recipe_supercomputer: '超级计算机',
  recipe_crystal_oscillator: '晶体振荡器',
  recipe_alt_cast_screw: '替代配方：铸造螺丝',
  recipe_alt_steel_screw: '替代配方：钢制螺丝',
  recipe_alt_iron_wire: '替代配方：铁制电线',
  recipe_alt_pure_iron_ingot: '替代配方：纯铁锭',
  recipe_alt_iron_alloy_ingot: '替代配方：合金铁锭',
  recipe_alt_copper_alloy_ingot: '替代配方：合金铜锭',
  recipe_alt_pure_copper_ingot: '替代配方：纯铜锭',
  recipe_alt_solid_steel_ingot: '替代配方：坚固钢锭',
  recipe_alt_coke_steel_ingot: '替代配方：石油焦钢锭',
  recipe_alt_steel_rod: '替代配方：钢制铁棒',
  recipe_alt_stitched_iron_plate: '替代配方：缝合铁板',
  recipe_alt_bolted_iron_plate: '替代配方：螺栓铁板',
  recipe_alt_steeled_frame: '替代配方：钢制框架',
  recipe_alt_encased_industrial_pipe: '替代配方：封装工业钢管',
  recipe_alt_heavy_encased_frame: '替代配方：重型封装框架',
  recipe_alt_caterium_circuit_board: '替代配方：钦金电路板',
  recipe_alt_silicon_circuit_board: '替代配方：硅电路板',
  recipe_alt_caterium_computer: '替代配方：钦金电脑',
  recipe_alt_crystal_computer: '替代配方：晶体电脑',
  recipe_alt_fused_wire: '替代配方：熔合电线',
  recipe_alt_fused_quickwire: '替代配方：熔合快速线',
  recipe_plastic: '塑料',
  recipe_rubber: '橡胶',
  recipe_fuel: '燃油',
  recipe_residual_plastic: '残余塑料',
  recipe_residual_rubber: '残余橡胶',
  recipe_petroleum_coke: '石油焦',
  recipe_residual_fuel: '残余燃油',
  recipe_compacted_coal: '压实煤',
  recipe_turbofuel: '涡轮燃油',
  recipe_black_powder: '黑火药',
  recipe_smokeless_powder: '无烟火药',
  recipe_packaged_water: '封装水',
  recipe_unpackaged_water: '拆包水',
  recipe_packaged_oil: '封装原油',
  recipe_packaged_fuel: '封装燃油',
  recipe_unpackaged_fuel: '拆包燃油',
  recipe_packaged_turbofuel: '封装涡轮燃油',
  recipe_packaged_rocket_fuel: '封装火箭燃料',
  recipe_rocket_fuel: '火箭燃料',
  recipe_ionized_fuel: '电离燃料',
  recipe_alumina_solution: '氧化铝溶液',
  recipe_aluminum_scrap: '铝废料',
  recipe_aluminum_ingot: '铝锭',
  recipe_alclad_aluminum_sheet: '覆铝板',
  recipe_aluminum_casing: '铝制外壳',
  recipe_heat_sink: '散热器',
  recipe_sulfuric_acid: '硫酸',
  recipe_nitric_acid: '硝酸',
  recipe_cooling_system: '冷却系统',
  recipe_battery: '电池',
  recipe_radio_control_unit: '无线电控制单元',
  recipe_fused_modular_frame: '熔合模块化框架',
  recipe_turbo_motor: '涡轮电机',
  recipe_electromagnetic_control_rod: '电磁控制棒',
  recipe_uranium_pellet: '铀颗粒',
  recipe_encased_uranium_cell: '封装铀电池',
  recipe_uranium_fuel_rod: '铀燃料棒',
  recipe_non_fissile_uranium: '非裂变铀',
  recipe_plutonium_pellet: '钚颗粒',
  recipe_encased_plutonium_cell: '封装钚电池',
  recipe_plutonium_fuel_rod: '钚燃料棒',
  recipe_copper_powder: '铜粉',
  recipe_pressure_conversion_cube: '压力转换立方体',
  recipe_nuclear_pasta: '核意面',
  recipe_reanimated_sam: '复活 SAM',
  recipe_sam_fluctuator: 'SAM 波动器',
  recipe_diamonds: '钻石',
  recipe_time_crystal: '时间晶体',
  recipe_dark_matter_crystal: '暗物质晶体',
  recipe_smart_plating: '智能嵌板',
  recipe_versatile_framework: '多用途框架',
  recipe_automated_wiring: '自动化线缆',
  recipe_modular_engine: '模块化发动机',
  recipe_adaptive_control_unit: '自适应控制单元',
  recipe_assembly_director_system: '装配主管系统',
  recipe_magnetic_field_generator: '磁场发生器',
  recipe_thermal_propulsion_rocket: '热推进火箭',
  recipe_ai_expansion_server: 'AI 扩展服务器',
  recipe_biochemical_sculptor: '生化雕刻器',
  recipe_ballistic_warp_drive: '弹道跃迁驱动器',
};

export function displayMaterialName(name?: string | null, gameKey?: string | null): string {
  if (gameKey && materialByKey[gameKey]) return materialByKey[gameKey];
  if (name && materialByEnglish[name]) return materialByEnglish[name];
  return name || '-';
}

export function displayMachineName(name?: string | null): string {
  if (!name) return '建筑';
  return machineByEnglish[name] || name;
}

export function displayRecipeName(recipe?: Pick<Recipe, 'gameKey' | 'name'> | null): string {
  if (!recipe) return '未选择配方';
  return recipeByKey[recipe.gameKey] || recipe.name.replace(/^Alternate: /, '替代配方：');
}

export function displayRecipeAmount(item: RecipeMaterialAmount): string {
  return `${displayMaterialName(item.materialName, item.materialGameKey)} ${formatAmount(item.amountPerMinuteAt100Percent)}`;
}

export function displayMaterialType(type?: MaterialType | null): string {
  return type ? materialTypeLabels[type] : '-';
}

export function displayTransportName(name?: string | null, type?: TransportType | null): string {
  if (!name) return '-';
  const translatedType = type ? transportTypeLabels[type] : '';
  return name
    .replace('Belt', '传送带')
    .replace('Pipe', '管道')
    .replace(/^Mk\./, 'Mk.')
    .replace('Pipeline', '管道') || translatedType;
}

export function displayTransportAdvice(advice?: TransportAdvice | null): string {
  if (!advice) return '';
  const type = transportTypeLabels[advice.transportType];
  const required = formatAmount(advice.requiredThroughput);
  const current = advice.currentMaxCapacity == null ? '未知' : `${formatAmount(advice.currentMaxCapacity)}/min`;
  if (advice.requiredThroughput <= 0.000001) return `暂无有效吞吐需求。`;
  if (!advice.recommendedLevel) return `需求 ${required}/min 超过所有已配置${type}等级，请拆线或补充更高科技数据。`;
  if (advice.overCurrentMax) return `需求 ${required}/min，当前最高承载 ${current}，建议升级到 ${displayTransportName(advice.recommendedName, advice.transportType)}。`;
  return `需求 ${required}/min，当前${type}等级足够。`;
}

export function displayWarning(warning: string): string {
  if (warning.startsWith('External source factory is disabled: ')) {
    return `外联来源工厂已禁用：${warning.replace('External source factory is disabled: ', '')}`;
  }
  if (warning === 'Net value is negative; this line cannot be used as an external output source.') {
    return '净值为负，这条线不能作为外联输出来源。';
  }
  if (warning === 'External input is higher than the active line requirement; frontend can mark the external input number yellow.') {
    return '外厂输入高于当前有效使用量，该输入数字可标黄，表示没有被完全使用。';
  }
  if (warning.includes('Required throughput exceeds all configured')) {
    return '吞吐需求超过所有已配置运输等级，请拆线或补充更高科技数据。';
  }
  if (warning.includes('Recommended upgrade to')) {
    return '当前最高运输等级无法承载这条线，建议升级运输等级。';
  }
  return warning;
}

export function displayExternalOption(option: ExternalSourceOption): string {
  return `${option.sourceFactoryName} / ${translateLineLikeName(option.sourceLineName)} · ${formatAmount(option.availableAmount)}/min`;
}

export function displayConnection(connection: ExternalConnection): string {
  return `${connection.sourceFactoryName} / ${translateLineLikeName(connection.sourceLineName)}`;
}

export function translateLineLikeName(name?: string | null): string {
  if (!name) return '-';
  return materialByEnglish[name] || name;
}

function formatAmount(value: number | null | undefined, digits = 2): string {
  if (value === null || value === undefined || Number.isNaN(value)) return '-';
  if (Math.abs(value) < 0.000001) return '0';
  const rounded = Number(value.toFixed(digits));
  return String(rounded);
}
