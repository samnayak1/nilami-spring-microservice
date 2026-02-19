import { z } from "zod";

export const BidEventSchema = z.object({
  eventId: z.string(),
  itemId: z.string(),
  bidId: z.string(),
  amount: z.number(),
  userId: z.string(),
 
  timestamp: z.coerce.date(), 
});

export type BidEvent = z.infer<typeof BidEventSchema>;