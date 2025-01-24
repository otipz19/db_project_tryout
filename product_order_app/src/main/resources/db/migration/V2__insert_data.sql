BEGIN;

INSERT INTO vendor(name, address)
VALUES
    ('Super Cool Chair Co.', '123 Fun Ave, Chair City'),
    ('Fantastic Furniture Factory', '456 Comfy Rd, Sofa Town'),
    ('Bizarre Bed and Beyond', '789 Dream St, Mattress Land');

INSERT INTO product(name, price, description, vendor_id)
VALUES
    ('Super Cool Chair', 99.99, 'A chair that makes you feel like you are sitting on clouds. Comes in flamingo pink and rocket red.', 1),
    ('Luxe Lounger', 149.99, 'The ultimate lounge experience. Includes cupholder and built-in mini-fridge.', 1),
    ('Fluffy Cloud Pillow', 29.99, 'For those who need their nap time to feel like a dreamy cloud embrace.', 2),
    ('Rainbow Beanbag', 79.99, 'Beanbag chair that doubles as a small trampoline for the adventurous souls.', 2),
    ('Sleepy Snuggler Bed', 399.99, 'A bed that wraps around you like a loving hug every night.', 3),
    ('Midnight Mattress', 199.99, 'For night owls who need to sleep during the day. Features built-in blackout curtains.', 3);

INSERT INTO orders(cost, description)
VALUES
    (279.97, 'Order for a comfy chair and a pillow, because nap time is serious business.'),
    (549.98, 'Order for a luxurious lounger and the Sleepy Snuggler Bed for ultimate relaxation.');

INSERT INTO product_orders(product_id, order_id, amount)
VALUES
    (1, 1, 2),  -- 2 Super Cool Chairs in order 1
    (4, 1, 1),  -- 1 Rainbow Beanbag in order 1
    (5, 2, 1),  -- 1 Sleepy Snuggler Bed in order 2
    (2, 2, 1);  -- 1 Luxe Lounger in order 2

COMMIT;
