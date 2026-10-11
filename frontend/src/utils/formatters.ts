export const formatIndianNumber = (num: number): string => {
  if (isNaN(num)) return '0';
  const isNegative = num < 0;
  const absNum = Math.abs(num);

  const integerPart = Math.floor(absNum);
  const fractionalPart = absNum - integerPart;

  const str = integerPart.toString();
  let result = '';

  if (str.length <= 3) {
    result = str;
  } else {
    const lastThree = str.substring(str.length - 3);
    const otherNumbers = str.substring(0, str.length - 3);
    const formattedOther = otherNumbers.replace(/\B(?=(\d{2})+(?!\d))/g, ',');
    result = `${formattedOther},${lastThree}`;
  }

  if (fractionalPart > 0) {
    result += `.${fractionalPart.toFixed(2).split('.')[1]}`;
  }

  return isNegative ? `-${result}` : result;
};
