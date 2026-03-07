ALTER TABLE petapp.pets
ADD COLUMN deleted_at TIMESTAMP;

-- Create partial index for active pets (not deleted)
CREATE INDEX idx_pets_owner_active ON petapp.pets(owner) WHERE deleted_at IS NULL;

-- Create partial index for finding active pets by ID
CREATE INDEX idx_pets_id_active ON petapp.pets(id) WHERE deleted_at IS NULL;
