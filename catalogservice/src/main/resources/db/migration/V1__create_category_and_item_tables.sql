
CREATE TABLE categories (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE items (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    base_price NUMERIC(19,2) NOT NULL,
    brand VARCHAR(255) NOT NULL,
    creator_user_id VARCHAR(255) NOT NULL,
    expiry_time TIMESTAMP NOT NULL,
    category_id UUID NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE item_pictures (
    item_id UUID NOT NULL REFERENCES items(id) ON DELETE CASCADE,
    picture_id VARCHAR(255) NOT NULL,
    PRIMARY KEY (item_id, picture_id)
);
