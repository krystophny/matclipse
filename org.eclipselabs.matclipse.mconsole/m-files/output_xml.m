function str=output_xml(data,orient,fid,vname,xml,init,varargin)
% 
% produces xml output of matlab variable data
% 
% usage to write files:
%   output_xml(data,orient,fid,vname,xml)
%   output_xml(data) - simplest version, output to screen
%   output_xml(data,[],[],1) - output to screen, only link to data
%
% usage for output to string
%   str = output_xml(data,orient,[],vname,xml);
%   str = output_xml(data); - simplest version
%   str = output_xml(data,[],[],1) - only link to data
%
% Output function for transfer to PYTHON and JAVA
%
% works on data with class: 
%           single, double, char, cell, struct, logical
%           inline, function_handle (also arrays of function handles)
%           int8, uint8, int16, uint16, int32, uint32, int64, uint64
%           dchar  (special class to store char as double) 
%           sparse (which is no class starting from 6.5)
%           sym
%
% not a perfect solution for int64 und uint64 !!!!!!!!!
%  - works without rounding error until 2^62 for int64 and 2^63 for uint64
%
% input:
%        data     any standard matlab class or array of handles
% optional input:
%        orient   either 't' or 'r' or [] 
%                 (default 't') 
%                 't' writes in line orientation (2nd dimension first)
%                 'r' writes in regular orientation
%                 has no influence when vname = 1
%        fid      either file name or valid file handle 
%                 (default 1, writing to screen)
%                 has no influence when vname = 1
%        vname    0: all data are fully put into output
%                 1: only links in form of Matlab-commands are given for
%                    the following classes: double, single, logical, int*
%                    and uint*
%                 (default 0, all data are given)
%        xml      xml-output
%                 (default 1, writing xml)
%        init     initialization status
%                 (for internal use only)
%
% If the output variable str is specified, the output is put into this
% character variable instead of an output file. (NEW)
%
% output on file or screen (fid=1) in the following format:
%
% 1: (matlab r a 
% 2: (double 4 3
% 3: 1 2 3 4 5 6 7 8 9 10 11 12 
% 4: /double)
% 5: /matlab)
% 
% 1: begin of matlab output, orientation, name of variable
%
% 2: begin of class, size vector of length ndims
%    class here can 
%           double, single, char, dchar, sparse,
%           logical, int8, uint8, int16, uint16, int32, uint32
%
% 3: one line of data
%    for the class double this can be followed by optional imaginary parts
%    1 2 3 4 5 6 7 8 9 10 11 12 :5 5 5 5 5 5 5 5 5 5 5 5 
%
%    for the class sparse the following notation applies
%    1 2 3 :5 2 1: 1.1 1.2.1.3: 1.0 1.0 1.0
%    line index: column index: real part: optional imaginary part 
%
% 4: end of class
% 5: end of matlab output
%
% So, this example represents the double array
%
%  1  4  7 10
%  2  5  8 11
%  3  6  9 12
%
% Watch the orientation: Here 'r' 
%
% Other classes might slightly differ in the 2nd line and
% instead of the third line start output for the content
%
% 2: (cell 3 2 4
%    followed by contents of cell, each content starting with 
%    its own class
%
% 2: (struct 2 2:key1 key2 key3 ...
%    followed for each key with all contents of the array
%
%    class of getfield(S,{1},key1)
%    class of getfield(S,{2},key1)
%     .      
%    class of getfield(S,{end},key1)
%    class of getfield(S,{1},key2)
%    class of getfield(S,{2},key2)
%     .      
%    class of getfield(S,{end},key2)
%     .
%     .
%
% 2: (inline
%    followed by class char with the formula
%    and by class cell containing classes char with arguments
%
% 2: (function_handle 2 1
%    followed by classes char with function names
%
% Remarks:
%
% This format is fully flexible in the sense of Matlab. 
% E.g., cells can contain doubles, chars, structures.
%
% An extension of the code to classes, e.g., defined in toolboxes
% is straightforward.
%
% The purpose of this routine is to have a fully transparent flow
% from Matlab data to Python or Java without any restrictions.
%
% For classe single and double now also the type ("real" or "complex") is
% specified in the header.
%
% Now also min max and mean is given for numeric arrays.

% Winfried Kernbichler 14.02.2011

% default orientation when nothing is provided
  orient_d = 't';
  % check input
  if nargin < 1, error('No data given'); end
  if nargin < 2 || isempty(orient), orient = orient_d; end
  orient = lower(orient(1));
  if ~any(strcmp(orient,{'t','r'})), error('Orientation not defined'); end
  if nargin < 3 || isempty(fid), fid = 1; end 
  if nargin < 4 || isempty(vname), vname = 0; end
  if nargin < 5 || isempty(xml), xml = 1; end
  if nargin < 6 || isempty(init), init = 1; end
  
  if nargout > 0, outfile = 0; else, outfile = 1; end
  
  % initialization mode with opening and closing of files if necesarry
  if init
    init = 0;
    if ~outfile, str = ''; end
    if ~ischar(vname)
        if vname
            vname = inputname(1);
            %orient = 'r';
        end
    end
    
    xml_pre = '<?xml version="1.0" encoding="iso-8859-1" ?>';
    if outfile & ischar(fid)
      fid  = fopen(fid,'w');
      file = 1;
    else
      file = 0;
    end
    if xml
      c_delim  = {'<matlab ','</matlab>'};
      if outfile
          fprintf(fid,[xml_pre,'\n']);
      else
          str = [str,sprintf([xml_pre,'\n'])];
      end
    else
      c_delim  = {'(matlab ','/matlab)'};
    end
    var_name = inputname(1);
    if isempty(var_name), var_name = 'undefined'; end
    if xml
        if outfile
            fprintf(fid,[c_delim{1},'name="',var_name,'" orientation="',orient,'">\n']);
        else
            str = [str,sprintf([c_delim{1},'name="',var_name,'" orientation="',orient,'">\n'])];
        end
    else
        if outfile
            fprintf(fid,[c_delim{1},orient,' ',var_name,' \n']);
        else
            str = [str,sprintf([c_delim{1},orient,' ',var_name,' \n'])];
        end
    end
    if outfile
        switch class(data)
            case 'inline'
                output_xml(data,orient,fid,vname,xml,init,'inline',var_name);
            otherwise
                output_xml(data,orient,fid,vname,xml,init);
        end
        %output_xml(data,orient,fid,vname,xml,init);
        fprintf(fid,[c_delim{2},'\n']);
    else
        switch class(data)
            case 'inline'
                str = output_xml(data,orient,str,vname,xml,init,'inline',var_name);
            otherwise
                str = output_xml(data,orient,str,vname,xml,init);
        end
        %str = output_xml(data,orient,str,vname,xml,init);
        str = [str,sprintf([c_delim{2}])];
    end
    if file
      fclose(fid);
    end
  else
    % find class of input data
    % also handles and arrays of only handles for graphics objects 
    % are treated as classes here
    % this is the only difference to the matlab philosophy
    if isempty(varargin)
      %hlp = ishandle(data);
      %if ~isempty(hlp) & all(hlp),
      %    c = 'handle';
      %else
      c = class(data);
      %end
      if issparse(data), c = 'sparse'; end
    else
      % forced input of class
      c = varargin{1};
    end
    
    % treatment of all classes
    r_format = '%.7g ';
    d_format = '%.15g ';
    i_format = '%i';
    c_split  = ':'; 
    c_eol    = '\n'; 
    orient   = orient(1);    
    if xml
      c_delim1  = {['<',c,' '],['\n</',c,'>\n']};
      c_delim2  = {['<',c,' '],['</',c,'>\n']};
    else
      c_delim1  = {['(',c,' '],['\n/',c,')\n']};
      c_delim2  = {['(',c,' '],['/',c,')\n']};
    end
    
    if ~outfile, str = fid; end
    
    switch c
      
     case 'double'
      c_delim = c_delim1;
      data = trans12(data,orient);
      r_data = real(data(:));
      if any(imag(data(:)))
          i_data = imag(data(:));
          dtype='complex'; 
      else
          dtype='real'; 
      end
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml,[],dtype);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              fprintf(fid,'%c',vname);              
          else
              d_format = optim_format(r_data);
              fprintf(fid,d_format,r_data);
              if strcmp(dtype,'complex')
                  d_format = optim_format(i_data);
                  d_format = strrep(d_format,'%','%+');
                  fprintf(fid,c_split); 
                  fprintf(fid,d_format,i_data); 
              end
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml,[],dtype);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              str = [str,sprintf('%c',vname)];              
          else
              d_format = optim_format(r_data);
              str = [str,sprintf(d_format,r_data)];
              if strcmp(dtype,'complex')
                  d_format = optim_format(i_data);
                  d_format = strrep(d_format,'%','%+');                  
                  str = [str,sprintf(c_split)]; 
                  str = [str,sprintf(d_format,i_data)]; 
              end
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case {'single'}
      c_delim = c_delim1;
      data = trans12(data,orient);
      r_data = real(data(:));
      if any(imag(data(:)))
          i_data = imag(data(:));
          dtype='complex'; 
      else
          dtype='real'; 
      end
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml,[],dtype);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              fprintf(fid,'%c',vname);              
          else
              d_format = optim_format(r_data);
              fprintf(fid,r_format,r_data);
              if strcmp(dtype,'complex')
                  d_format = optim_format(i_data);
                  d_format = strrep(d_format,'%','%+');
                  fprintf(fid,c_split); 
                  fprintf(fid,r_format,i_data); 
              end
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml,[],dtype);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              str = [str,sprintf('%c',vname)];              
          else
              d_format = optim_format(r_data);
              str = [str,sprintf(r_format,r_data)];
              if strcmp(dtype,'complex')
                  d_format = optim_format(i_data);
                  d_format = strrep(d_format,'%','%+');
                  str = [str,sprintf(c_split)]; 
                  str = [str,sprintf(r_format,i_data)]; 
              end
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'dchar'
      c_delim = c_delim1;
      data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml);
          fprintf(fid,d_format,data(:));
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml);
          str = [str,sprintf(d_format,data(:))];
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'logical'
      c_delim = c_delim1;
      data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              fprintf(fid,'%c',vname);              
          else
              fprintf(fid,d_format,data(:));
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              str = [str,sprintf('%c',vname)];              
          else
              str = [str,sprintf(d_format,data(:))];
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case {'int8','uint8','int16','uint16','int32','uint32'}
      c_delim = c_delim1;
      data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              fprintf(fid,'%c',vname);              
          else
              fprintf(fid,'%i ',data(:));
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              str = [str,sprintf('%c',vname)];              
          else
              str = [str,sprintf('%i ',data(:))];
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case {'int64','uint64'}
      % the format is not perfect for real high nummbers
      % at the moment there is nothing better in Matlab
      c_delim = c_delim1;
      data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              fprintf(fid,'%c',vname);              
          else
              fprintf(fid,'%0.0f ',data(:));
              %fprintf(fid,'%i ',data(:));
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml);
          if ischar(vname)
              vname = ['<![CDATA[',vname,']]>'].';
              str = [str,sprintf('%c',vname)];              
          else
              str = [str,sprintf('%0.0f ',data(:))];
              %str = [str,sprintf('%i ',data(:))];
          end
          str = [str,sprintf(c_delim{2})];
      end
      
%      case 'handle'
%       c_delim = c_delim1;
%       data = trans12(data,orient);
%       rp = data(:);
%       if outfile
%           fprintf(fid,c_delim{1});
%           printsize(fid,data,c_eol,xml);
%           fprintf(fid,d_format,rp);
%           fprintf(fid,c_delim{2});
%       else
%           str = [str,sprintf(c_delim{1})];
%           str = printsize(str,data,c_eol,xml);
%           str = [str,sprintf(d_format,rp)];
%           str = [str,sprintf(c_delim{2})];
%       end
      
     case 'char'
      % linefeed char(10) must be converted to \n
      % only possible for 1-D arrays not for n-D char arrays
      % choice here is to switch to dchar
      % a artificial class with char stored as double
      c_delim = c_delim1;
      if ~isempty(strfind(transpose(data(:)),char(10)))
          if outfile
              output_xml(double(data),orient,fid,vname,xml,init,'dchar')
          else
              str = output_xml(double(data),orient,str,vname,xml,init,'dchar');
          end
      else
        data = trans12(data,orient);
        if outfile
            fprintf(fid,c_delim{1});
            printsize(fid,data,c_eol,xml,vname);
        else
            str = [str,sprintf(c_delim{1})];
            str = printsize(str,data,c_eol,xml,vname);
        end
        data = data(:).';
        data = ['<![CDATA[',data,']]>'];
        data = data.';
        if outfile
            fprintf(fid,'%c',data);
            fprintf(fid,c_delim{2});
        else
            str = [str,sprintf('%c',data)];
            str = [str,sprintf(c_delim{2})];
        end
      end
      
     case 'cell'
      c_delim = c_delim2;
      data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml,vname);
          for k = 1:numel(data)
              vn = vname;
              if ischar(vname), vn = [vname,'{',int2str(k),'}']; end
              output_xml(data{k},orient,fid,vn,xml,init);
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml,vname);
          for k = 1:numel(data)
              vn = vname;
              if ischar(vname), vn = [vname,'{',int2str(k),'}']; end
              str = output_xml(data{k},orient,str,vn,xml,init);
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'struct'
      c_delim = c_delim2;
      data = trans12(data,orient);
      field = fieldnames(data); lf = length(field);    
      if outfile
          fprintf(fid,c_delim{1});
          if xml
              fname = '';
              for k = 1:lf
                  fnamea = sprintf('%c',field{k});
                  if k ~= lf, fnamea=[fnamea,',']; end
                  fname = [fname,fnamea];
              end
              printsize(fid,data,c_eol,xml,fname);
          else
              printsize(fid,data,c_split,xml);
              for k = 1:lf
                  fprintf(fid,'%c',field{k});
                  if k ~= lf, fprintf(fid,' '); end
              end
              fprintf(fid,c_eol);
          end
          for k = 1:lf
              for l = 1:numel(data)
                  vn = vname;
                  if ischar(vname), vn = [vname,'(',int2str(l),').',field{k}]; end
                  output_xml(getfield(data,{l},field{k}),orient,fid,vn,xml,init);
              end
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          if xml
              fname = '';
              for k = 1:lf
                  fnamea = sprintf('%c',field{k});
                  if k ~= lf, fnamea=[fnamea,',']; end
                  fname = [fname,fnamea];
              end
              str = printsize(str,data,c_eol,xml,fname);
          else
              str = printsize(str,data,c_split,xml);
              for k = 1:lf
                  str = [str,sprintf('%c',field{k})];
                  if k ~= lf, str = [str,sprintf(' ')]; end
              end
              str = [str,sprintf(c_eol)];
          end
          for k = 1:lf
              for l = 1:numel(data)
                  vn = vname;
                  if ischar(vname), vn = [vname,'(',int2str(l),').',field{k}]; end
                  str = output_xml(getfield(data,{l},field{k}),orient,str,vn,xml,init);
              end
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'sparse'
      c_delim = c_delim1;
      [si,sj,sv] = find(data); rp = real(sv); ip = imag(sv);
      if any(imag(sv(:)))
          dtype='complex'; 
      else
          dtype='real'; 
      end

      if outfile
          fprintf(fid,c_delim{1});
          %printsize(fid,data,c_eol,xml);
          printsize(fid,data,c_eol,xml,[],dtype);
          fprintf(fid,'%i ',si);
          fprintf(fid,c_split);
          fprintf(fid,'%i ',sj);
          fprintf(fid,c_split);
          d_format = optim_format(rp);
          fprintf(fid,d_format,rp);
          if any(ip)
              fprintf(fid,c_split); 
              d_format = optim_format(ip); 
              d_format = strrep(d_format,'%','%+');
              fprintf(fid,d_format,ip); 
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          %str = printsize(str,data,c_eol,xml);
          str = printsize(fid,data,c_eol,xml,[],dtype);
          str = [str,sprintf('%i ',si)];
          str = [str,sprintf(c_split)];
          str = [str,sprintf('%i ',sj)];
          str = [str,sprintf(c_split)];
          d_format = optim_format(rp);
          str = [str,sprintf(d_format,rp)];
          if any(ip)
              str = [str,sprintf(c_split)]; 
              d_format = optim_format(ip);                   
              d_format = strrep(d_format,'%','%+');
              str = [str,sprintf(d_format,ip)]; 
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'function_handle'
      c_delim = c_delim2;
      %data = trans12(data,orient);
      if outfile
          fprintf(fid,c_delim{1});
          printsize(fid,data,c_eol,xml);
          for k = 1:numel(data)
              if numel(data) == 1
                  fhan = functions(data);
              else
                  fhan = functions(data(k));
              end
              fhanm = fhan.function;
              output_xml(fhanm,orient,fid,vname,xml,init);
          end
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf(c_delim{1})];
          str = printsize(str,data,c_eol,xml);
          for k = 1:numel(data)
              if numel(data) == 1
                  fhan = functions(data);
              else
                  fhan = functions(data(k));
              end
              fhanm = fhan.function;
              str = output_xml(fhanm,orient,str,vname,xml,init);
          end
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'inline'
      c_delim = c_delim2;
      form = formula(data);
      argn = argnames(data);
      
      inline_char = '';
      if length(varargin) > 1, inline_char = varargin{2}; end
      for k = 1:length(argn)
          if k == 1
              inline_char = [inline_char,'(',argn{k}];
          else
              inline_char = [inline_char,',',argn{k}];
          end
      end
      inline_char = [inline_char,') = ',form];

      if outfile
          %fprintf(fid,[c_delim{1},'\n']);
          fprintf(fid,[c_delim{1}]);
          printsize(fid,data,c_eol,xml);
          %output_xml(form,orient,fid,vname,xml,init);
          %output_xml(argn,orient,fid,vname,xml,init);
          output_xml(inline_char,orient,fid,vname,xml,init);
          fprintf(fid,c_delim{2});
      else
          %str = [str,sprintf([c_delim{1},'\n'])];
          str = [str,sprintf([c_delim{1}])];
          str = printsize(str,data,c_eol,xml);
          %str = output_xml(form,orient,str,vname,xml,init);
          %str = output_xml(argn,orient,str,vname,xml,init);
          str = output_xml(inline_char,orient,fid,vname,xml,init);
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'sym'
      c_delim = c_delim2;
      form = char(data);
      if outfile
          fprintf(fid,[c_delim{1}]);
          printsize(fid,data,c_eol,xml);
          output_xml(form,orient,fid,vname,xml,init);
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf([c_delim{1}])];
          str = printsize(str,data,c_eol,xml);
          str = output_xml(form,orient,str,vname,xml,init);
          str = [str,sprintf(c_delim{2})];
      end
      
     case 'MException'
      c_delim = c_delim2;
      form = '';
      ident = data.identifier;
      messa = data.message;
      stack = data.stack;
      cause = data.cause;
      if outfile
          fprintf(fid,[c_delim{1}]);
          printsize(fid,data,c_eol,xml);
          output_xml(ident,orient,fid,'identifier',xml,init); % vname
          output_xml(messa,orient,fid,'message',xml,init);
          output_xml(stack,orient,fid,'stack',xml,init);
          output_xml(cause,orient,fid,'cause',xml,init);
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf([c_delim{1}])];
          str = printsize(str,data,c_eol,xml);
          str = output_xml(ident,orient,str,'identifier',xml,init);
          str = output_xml(messa,orient,str,'message',xml,init);
          str = output_xml(stack,orient,str,'stack',xml,init);
          str = output_xml(cause,orient,str,'cause',xml,init);
          str = [str,sprintf(c_delim{2})];
      end
      
     otherwise
      % liefert jetzt einen leeren String      
      c_delim = c_delim2;
      form = ['not supported class: ',c]; 
      if outfile
          fprintf(fid,[c_delim{1}]);
          printsize(fid,data,c_eol,xml);
          output_xml(form,orient,fid,vname,xml,init);
          fprintf(fid,c_delim{2});
      else
          str = [str,sprintf([c_delim{1}])];
          str = printsize(str,data,c_eol,xml);
          str = output_xml(form,orient,str,vname,xml,init);
          str = [str,sprintf(c_delim{2})];
      end
            
      %error(['nothing defined for class ',upper(c)]);
      
    end
    
  end
  
function res = trans12(data,orient)
% transposes the first two dimensions of an array only if
% orient is set to 't'
%
% for 2-D arrays it is the same as transpose
  if strcmp(orient,'t')
    sw = class(data);
    switch sw
     case {'int64','uint64'}
      % permute does not work for these cases
      siz = size(data); siz(1:2) = siz(2:-1:1);
      dim = ndims(data);
      res = zeros(siz,sw);
      estr = ['res(k',repmat(',:',1,dim-1),') = data(:,k',repmat(',:',1,dim-2),');'];
      for k = 1:siz(1)
        eval(estr);
      end
     otherwise
      p = [1:ndims(data)]; p(1:2)=p(2:-1:1); res = permute(data,p);
    end
  else
    res = data;
  end
  
function str = printsize(fid,data,end_mark,xml,name,dtype)
% prints size of class
  if nargin < 4, xml = 0; end
  if nargin < 5 || isempty(name) || ~ischar(name), name = 'undefined'; end
  if nargin < 6, dtype = []; end
  if nargout > 0, str = fid; end
  
  s = size(data); ls = length(s);
  if isnumeric(data) && numel(data) > 1
      d_min  = num2str(min(data(:)));
      d_max  = num2str(max(data(:)));
      d_mean = num2str(mean(data(:)));
  else
      [d_min,d_max,d_mean] = deal('');
  end
  if xml
      if nargout == 0
          fprintf(fid,['name="',name,'" size="[']);
          for k = 1:ls
              fprintf(fid,'%i',s(k));
              if k ~= ls, fprintf(fid,','); else, fprintf(fid,']"'); end
          end
          if ~isempty(d_min),  fprintf(fid,[' min="',d_min,'"']); end
          if ~isempty(d_max),  fprintf(fid,[' max="',d_max,'"']); end
          if ~isempty(d_mean), fprintf(fid,[' mean="',d_mean,'"']); end
          if ~isempty(dtype) fprintf(fid,[' type="',dtype,'"']); end
          fprintf(fid,['>',end_mark]);
      else
          str = [str,sprintf(['name="',name,'" size="['])];
          for k = 1:ls
              str = [str,sprintf('%i',s(k))];
              if k ~= ls, str = [str,sprintf(',')]; else, str = [str,sprintf(']"')]; end
          end
          if ~isempty(d_min),  str = [str,sprintf([' min="',d_min,'"'])]; end
          if ~isempty(d_max),  str = [str,sprintf([' max="',d_max,'"'])]; end
          if ~isempty(d_mean), str = [str,sprintf([' mean="',d_mean,'"'])]; end
          if ~isempty(dtype) str = [str,sprintf([' type="',dtype,'"'])]; end
          str = [str,sprintf(['>',end_mark])];          
      end
  else
      if nargout == 0
          for k = 1:ls
              fprintf(fid,'%i',s(k));
              if k ~= ls, fprintf(fid,' '); else, fprintf(fid,end_mark); end
          end
      else
          for k = 1:ls
              str = [str,sprintf('%i',s(k))];
              if k ~= ls, str = [str,sprintf(' ')]; else, str = [str,sprintf(end_mark)]; end
          end
      end
  end

  
function f = optim_format(d,nl,acc)
% tries do compute a good general format for a double array 

if nargin < 2 || isempty(nl), nl = 0; end
if nl, lfc = '\n'; else, lfc = ' '; end
if nargin < 3 || isempty(acc)
    cl = class(d);
    if strcmp(cl,'double')
        acc = 14;
    else
        acc = 7;
    end
end
acc_s = int2str(acc);

if isempty(d)
    f = '[]';
    return
end

if numel(d) > 1.e5;
    f = ['% ','.',acc_s,'g',lfc];
    return
end

d = abs(d(:));
d_inf = isinf(d);
d(d_inf) = [];
d_nan = isnan(d);
d(d_nan) = [];
all_i = all((d - fix(d)) == 0);

if all_i
    max_d = max(abs(d));
    if max_d == 0
        dig = 3;
    else
        dig = ceil(log10(max_d))+2;
    end
    if any(d_inf) || any(d_nan), dig = max(dig,4); end
    dig = int2str(dig);
    if max_d < 1.e10
        %f = ['% ',dig,'d',lfc]; % Mit Ausrichtung
        f = ['%d',lfc];
    else
        %f = ['% ','.',acc_s,'g',lfc]; % Mit Ausrichtung
        f = ['%','.',acc_s,'g',lfc];
    end
else
    d(d==0) = [];
    max_d = max(abs(d));
    min_d = min(abs(d));
    del_d = max_d - min_d;
    dig = int2str(ceil(log10(max_d))+1);
    pow = fix(log10(d+eps));
    pow(pow<=0) = pow(pow<=0) - 1;
    max_pow = max(pow);
    
    str = sprintf(['% .',acc_s,'e'],d);
    p1 = regexp(str,'\.');
    p2 = regexp(str,'e');
    len = 0;
    lene = 0;
    for k = 1:numel(p1)
        str_k = str(p1(k)+1:p2(k)-1);
        pz = regexp(str_k,'[^0]'); if isempty(pz), pz = 0; end
        len = max(len,pz(end)-pow(k));
        lene = max(lene,pz(end));
        if lene == acc, break; end
    end
    flength = max(max_pow,2) + len + 3;
    flength = int2str(flength);
    len = int2str(len);
    lene = int2str(lene);
    
    
    if max_d <= 1.e8 && min_d >= 1e-8
        %f = ['% ',flength,'.',len,'f',lfc];
        f = ['%','.',len,'f',lfc];
    else
        %f = ['% ','.',lene,'e',lfc];
        f = ['%','.',lene,'e',lfc];
    end    
end  
  
  