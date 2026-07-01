const count = Number(process.env.COUNT || process.argv[2] || 1000);
const start = Number(process.env.START || process.argv[3] || 1);
const emailDomain = process.env.EMAIL_DOMAIN || "test.umc.it.kr";
const password = process.env.PASSWORD || "password12!";
const clientType = process.env.CLIENT_TYPE || "WEB";

const users = Array.from({length: count}, (_, index) => {
  const sequence = String(start + index).padStart(4, "0");
  return {
    email: `alpha_user_${sequence}@${emailDomain}`,
    password,
    clientType,
  };
});

process.stdout.write(`${JSON.stringify(users, null, 2)}\n`);
