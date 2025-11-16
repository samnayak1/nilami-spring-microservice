import { z } from "zod";

export const BidEventSchema = z.object({
  itemId: z.string(),
  bidId: z.string(),
  amount: z.number(),
  userId: z.string(),
  timestamp: z.string(),
});

export type BidEvent = z.infer<typeof BidEventSchema>;