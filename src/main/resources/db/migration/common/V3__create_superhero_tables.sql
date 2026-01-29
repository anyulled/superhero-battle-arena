-- Core superhero table with essential lookup fields
CREATE TABLE superheroes (
    id INT PRIMARY KEY,              -- Original superhero-api ID (not auto-generated)
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL,      -- Slug might not be unique globally but usually is, keeping NOT NULL. Removed UNIQUE constraint to be safe against data issues, or keep if strict. Original plan said UNIQUE. Let's keep UNIQUE.
    alignment VARCHAR(50),           -- good/bad/neutral
    publisher VARCHAR(100),
    created_at TIMESTAMPTZ DEFAULT NOW(),
    CONSTRAINT uk_superheroes_slug UNIQUE (slug)
);

CREATE INDEX idx_superheroes_name ON superheroes(name);
CREATE INDEX idx_superheroes_alignment ON superheroes(alignment);
CREATE INDEX idx_superheroes_publisher ON superheroes(publisher);

-- Power statistics (1:1)
CREATE TABLE superhero_powerstats (
    superhero_id INT PRIMARY KEY REFERENCES superheroes(id) ON DELETE CASCADE,
    intelligence INT DEFAULT 0,
    strength INT DEFAULT 0,
    speed INT DEFAULT 0,
    durability INT DEFAULT 0,
    power INT DEFAULT 0,
    combat INT DEFAULT 0,
    cost INT DEFAULT 10              -- Budget cost for the game
);

-- Appearance details (1:1)
CREATE TABLE superhero_appearance (
    superhero_id INT PRIMARY KEY REFERENCES superheroes(id) ON DELETE CASCADE,
    gender VARCHAR(50),
    race VARCHAR(100),
    height_cm INT,
    weight_kg INT,
    eye_color VARCHAR(50),
    hair_color VARCHAR(50)
);

-- Biography/Origin information (1:1)
CREATE TABLE superhero_biography (
    superhero_id INT PRIMARY KEY REFERENCES superheroes(id) ON DELETE CASCADE,
    full_name VARCHAR(255),
    aliases JSONB DEFAULT '[]',       -- Store as JSON array
    place_of_birth VARCHAR(255),
    first_appearance VARCHAR(255)
);

-- Image URLs (1:1)
CREATE TABLE superhero_images (
    superhero_id INT PRIMARY KEY REFERENCES superheroes(id) ON DELETE CASCADE,
    xs_url VARCHAR(500),
    sm_url VARCHAR(500),
    md_url VARCHAR(500),
    lg_url VARCHAR(500)
);

-- Add foreign key from hero_usage to superheroes
-- Note: hero_usage.hero_id might have values not in superheroes if distinct sources, but assuming consistent IDs.
-- If existing data in hero_usage violates this, migration will fail. Assuming clean state or consistent data.
ALTER TABLE hero_usage ADD CONSTRAINT fk_hero_usage_superhero 
    FOREIGN KEY (hero_id) REFERENCES superheroes(id);
