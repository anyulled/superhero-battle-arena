-- Add indexes for advanced hero search performance

-- Index on powerstats for range queries (min/max filters)
CREATE INDEX idx_powerstats_cost ON superhero_powerstats(cost);
CREATE INDEX idx_powerstats_power ON superhero_powerstats(power);
CREATE INDEX idx_powerstats_strength ON superhero_powerstats(strength);
CREATE INDEX idx_powerstats_speed ON superhero_powerstats(speed);
CREATE INDEX idx_powerstats_intelligence ON superhero_powerstats(intelligence);
CREATE INDEX idx_powerstats_durability ON superhero_powerstats(durability);
CREATE INDEX idx_powerstats_combat ON superhero_powerstats(combat);

-- Composite index for common search patterns
CREATE INDEX idx_powerstats_cost_power ON superhero_powerstats(cost, power);

-- Index on appearance for gender and race filters
CREATE INDEX idx_appearance_gender ON superhero_appearance(gender);
CREATE INDEX idx_appearance_race ON superhero_appearance(race);
CREATE INDEX idx_appearance_gender_race ON superhero_appearance(gender, race);

-- Composite index on superheroes for combined filters
CREATE INDEX idx_superheroes_alignment_publisher ON superheroes(alignment, publisher);
