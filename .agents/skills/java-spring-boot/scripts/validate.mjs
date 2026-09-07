import { resolve } from 'node:path';
import { runValidation } from '../../skill-creator/scripts/quick_validate.mjs';

process.exitCode = await runValidation(resolve(import.meta.dirname, '..'));
